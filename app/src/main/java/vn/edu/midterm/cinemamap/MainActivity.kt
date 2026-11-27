package vn.edu.midterm.cinemamap

import android.content.res.Configuration
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import vn.edu.midterm.cinemamap.map.CinemaMapView
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.model.SeatType
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.ui.SelectedSeatItem
import vn.edu.midterm.cinemamap.ui.SeatStatusFilter
import vn.edu.midterm.cinemamap.ui.BookingViewModel
import vn.edu.midterm.cinemamap.ui.SelectedSeatAdapter
import vn.edu.midterm.cinemamap.ui.typeLabelResource
import vn.edu.midterm.cinemamap.preview.CinemaPreviewActivity
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: BookingViewModel by viewModels()
    private val previewLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val id = result.data?.getStringExtra(CinemaPreviewActivity.EXTRA_SEAT_ID)
            MockCinemaData.rows.asSequence().flatMap { it.seats.asSequence() }
                .firstOrNull { it.id == id }?.let { seat ->
                    viewModel.inspectSeat(seat)
                    findViewById<CinemaMapView>(R.id.cinema_map)?.let { map ->
                        map.doOnLayout { map.post { map.locateSeat(seat.id) } }
                    }
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(0, 0), navigationBarStyle = SystemBarStyle.light(0, 0))
        setContentView(R.layout.activity_main)

        val cinemaMap = findViewById<CinemaMapView>(R.id.cinema_map)
        val content = findViewById<View>(R.id.booking_content)
        val sheet = findViewById<LinearLayout>(R.id.booking_sheet)
        val header = findViewById<View>(R.id.sheet_header)
        val legend = findViewById<View>(R.id.seat_legend)
        setupLegend()
        setupFilters()
        val behavior = BottomSheetBehavior.from(sheet)
        val toggle = findViewById<MaterialButton>(R.id.toggle_sheet)
        val summary = findViewById<TextView>(R.id.booking_summary)
        val totalPrice = findViewById<TextView>(R.id.booking_total_price)
        val inspected = findViewById<TextView>(R.id.inspected_seat)
        val preview = findViewById<MaterialButton>(R.id.open_3d_preview)
        val empty = findViewById<View>(R.id.empty_selection)
        val list = findViewById<RecyclerView>(R.id.selected_seats)
        val typeFilter = findViewById<MaterialButton>(R.id.filter_type)
        val statusFilter = findViewById<MaterialButton>(R.id.filter_status)
        val clearFilters = findViewById<View>(R.id.clear_filters)
        val emptyFilterMessage = findViewById<TextView>(R.id.filter_empty_message)
        val allSeats = MockCinemaData.rows.flatMap { it.seats }
        val money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
        val adapter = SelectedSeatAdapter(
            onLocate = { seat ->
                viewModel.inspectSeat(seat)
                cinemaMap.locateSeat(seat.id)
            },
            onRemove = viewModel::removeSeat,
            onPreview = { seat -> openSeatPreview(seat.id) }
        )
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        list.itemAnimator = null
        ViewCompat.setAccessibilityPaneTitle(sheet, getString(R.string.expand_sheet))

        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        fun updateSheetToggle(state: Int) {
            toggle.setText(if (state == BottomSheetBehavior.STATE_EXPANDED) R.string.collapse_sheet
                else R.string.expand_sheet)
        }
        fun updateMapHeight() {
            if (landscape) return
            // Apply after CoordinatorLayout finishes positioning its children.
            cinemaMap.post {
                // Legend occupies the strip immediately above the sheet.
                val legendTop = (sheet.top - legend.height).coerceAtLeast(0)
                legend.translationY = legendTop.toFloat()
                val availableHeight = legendTop.coerceIn(1, content.height.coerceAtLeast(1))
                if (cinemaMap.layoutParams.height != availableHeight) {
                    cinemaMap.updateLayoutParams { height = availableHeight }
                }
            }
        }

        fun sizeSheet() {
            if (content.height <= 0 || header.height <= 0) return
            // Leave a usable map above the expanded sheet, including in landscape.
            val sheetHeight = if (landscape) content.height else maxOf((content.height * 0.48f).toInt(), header.height + (48 * resources.displayMetrics.density).toInt())
                .coerceAtMost((content.height * 0.65f).toInt())
            if (sheet.layoutParams.height != sheetHeight) {
                sheet.updateLayoutParams { height = sheetHeight }
            }
            behavior.peekHeight = header.height.coerceAtMost(sheetHeight - 1)
        }

        legend.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateMapHeight() }
        header.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> sizeSheet() }
        content.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> sizeSheet() }
        sheet.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateMapHeight() }
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) = updateMapHeight()

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED ||
                    newState == BottomSheetBehavior.STATE_COLLAPSED
                ) {
                    updateSheetToggle(newState)
                }
                updateMapHeight()
            }
        })
        toggle.setOnClickListener {
            behavior.state = if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_EXPANDED
            }
        }
        // Restoring Behavior state may happen before our callback is registered.
        sheet.doOnLayout {
            sizeSheet()
            updateSheetToggle(behavior.state)
        }
        findViewById<View>(R.id.reset_view).setOnClickListener { cinemaMap.resetView() }
        preview.setOnClickListener {
            viewModel.uiState.value.inspectedSeat?.let { openSeatPreview(it.id) }
        }
        cinemaMap.onSeatClickListener = viewModel::onSeatTapped

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val selectedIds = state.selectedSeats.map { it.id }.toSet()
                    val matchingIds = allSeats.filter { state.filter.matches(it, it.id in selectedIds) }
                        .map { it.id }.toSet()
                    cinemaMap.selectedSeatIds = selectedIds
                    cinemaMap.matchingSeatIds = matchingIds
                    typeFilter.text = getString(R.string.filter_type, getString(typeLabel(state.filter.type)))
                    statusFilter.text = getString(R.string.filter_status, getString(statusLabel(state.filter.status)))
                    clearFilters.isEnabled = state.filter.isActive
                    emptyFilterMessage.isVisible = matchingIds.isEmpty()
                    summary.text = getString(
                        R.string.selection_summary, state.totalSeatsCount, state.totalSpotsCount
                    )
                    val formattedPrice = money.format(state.totalPriceVnd)
                    totalPrice.text = formattedPrice
                    totalPrice.contentDescription = getString(R.string.selection_total_price_description, formattedPrice)
                    adapter.submitList(state.selectedSeats.map { seat ->
                        SelectedSeatItem(seat, seat.id !in matchingIds)
                    })
                    empty.isVisible = state.selectedSeats.isEmpty()
                    val seat = state.inspectedSeat
                    preview.isEnabled = seat != null
                    preview.text = if (seat == null) getString(R.string.preview_3d_open)
                        else getString(R.string.preview_3d_open_seat, seat.id)
                    inspected.text = if (seat == null) {
                        getString(R.string.no_inspected_seat)
                    } else {
                        val status = when {
                            seat.id in cinemaMap.selectedSeatIds -> R.string.seat_selected
                            seat.availability == SeatAvailability.BOOKED -> R.string.seat_booked
                            seat.availability == SeatAvailability.UNAVAILABLE -> R.string.seat_unavailable
                            else -> R.string.seat_available
                        }
                        val description = getString(
                            R.string.inspected_seat, seat.id,
                            getString(seat.typeLabelResource()), getString(status)
                        )
                        if (seat.id !in matchingIds) description + getString(R.string.outside_filter_suffix)
                        else description
                    }
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }
    private fun openSeatPreview(id: String) {
        val seat = MockCinemaData.rows.asSequence().flatMap { it.seats.asSequence() }
            .firstOrNull { it.id == id } ?: return
        viewModel.inspectSeat(seat)
        previewLauncher.launch(Intent(this, CinemaPreviewActivity::class.java)
            .putExtra(CinemaPreviewActivity.EXTRA_SEAT_ID, id)
            .putStringArrayListExtra(CinemaPreviewActivity.EXTRA_SELECTED_IDS,
                ArrayList(viewModel.uiState.value.selectedSeats.map { it.id })))
    }

    private fun typeLabel(type: SeatType?): Int = when (type) {
        null -> R.string.filter_all
        SeatType.STANDARD -> R.string.seat_standard
        SeatType.VIP -> R.string.seat_vip
        SeatType.COUPLE -> R.string.seat_couple
    }

    private fun statusLabel(status: SeatStatusFilter?): Int = when (status) {
        null -> R.string.filter_all
        SeatStatusFilter.AVAILABLE -> R.string.seat_available
        SeatStatusFilter.SELECTED -> R.string.seat_selected
        SeatStatusFilter.BOOKED -> R.string.seat_booked
        SeatStatusFilter.UNAVAILABLE -> R.string.seat_unavailable
    }

    private fun setupFilters() {
        findViewById<View>(R.id.filter_type).setOnClickListener {
            val options = listOf(null, SeatType.STANDARD, SeatType.VIP, SeatType.COUPLE)
            val labels = options.map { getString(typeLabel(it)) }.toTypedArray()
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.filter_type_title)
                .setSingleChoiceItems(labels, options.indexOf(viewModel.uiState.value.filter.type)) { dialog, which ->
                    viewModel.setTypeFilter(options[which])
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.legend_close, null)
                .show()
        }
        findViewById<View>(R.id.filter_status).setOnClickListener {
            val options = listOf(null) + SeatStatusFilter.values().toList()
            val labels = options.map { getString(statusLabel(it)) }.toTypedArray()
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.filter_status_title)
                .setSingleChoiceItems(labels, options.indexOf(viewModel.uiState.value.filter.status)) { dialog, which ->
                    viewModel.setStatusFilter(options[which])
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.legend_close, null)
                .show()
        }
        findViewById<View>(R.id.clear_filters).setOnClickListener { viewModel.clearFilters() }
    }

    private fun setupLegend() {
        val items = findViewById<LinearLayout>(R.id.legend_items)
        findViewById<HorizontalScrollView>(R.id.legend_scroll).doOnLayout { it.scrollTo(0, 0) }
        val density = resources.displayMetrics.density

        fun addItem(labelResource: Int, colorResource: Int) {
            val item = LayoutInflater.from(this).inflate(R.layout.item_seat_legend, items, false)
            val sample = item.findViewById<TextView>(R.id.legend_sample)
            sample.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 4f * density
                setColor(getColor(colorResource))
            }
            if (labelResource == R.string.seat_couple) {
                sample.updateLayoutParams { width = (44 * density).toInt() }
            }
            item.findViewById<TextView>(R.id.legend_label).setText(labelResource)
            items.addView(item)
        }

        addItem(R.string.seat_standard, R.color.seat_standard)
        addItem(R.string.seat_vip, R.color.seat_vip)
        addItem(R.string.seat_couple, R.color.seat_couple)
        addItem(R.string.seat_selected, R.color.seat_accent)
        addItem(R.string.seat_booked, R.color.seat_occupied)
        addItem(R.string.seat_unavailable, R.color.seat_unavailable)

        findViewById<View>(R.id.legend_details).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getText(R.string.legend_title))
                .setMessage(getText(R.string.legend_description))
                .setPositiveButton(R.string.legend_close, null)
                .show()
        }
    }

}
