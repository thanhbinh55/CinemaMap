package vn.edu.midterm.cinemamap

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import kotlinx.coroutines.launch
import vn.edu.midterm.cinemamap.map.CinemaMapView
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.ui.BookingViewModel
import vn.edu.midterm.cinemamap.ui.SelectedSeatAdapter
import vn.edu.midterm.cinemamap.ui.typeLabelResource
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: BookingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val cinemaMap = findViewById<CinemaMapView>(R.id.cinema_map)
        val content = findViewById<View>(R.id.booking_content)
        val sheet = findViewById<LinearLayout>(R.id.booking_sheet)
        val header = findViewById<View>(R.id.sheet_header)
        val behavior = BottomSheetBehavior.from(sheet)
        val toggle = findViewById<MaterialButton>(R.id.toggle_sheet)
        val summary = findViewById<TextView>(R.id.booking_summary)
        val inspected = findViewById<TextView>(R.id.inspected_seat)
        val empty = findViewById<View>(R.id.empty_selection)
        val list = findViewById<RecyclerView>(R.id.selected_seats)
        val money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
        val adapter = SelectedSeatAdapter(
            onLocate = { seat -> cinemaMap.locateSeat(seat.id) },
            onRemove = viewModel::removeSeat
        )
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        list.itemAnimator = null
        ViewCompat.setAccessibilityPaneTitle(sheet, getString(R.string.expand_sheet))

        fun updateMapHeight() {
            // Apply after CoordinatorLayout finishes positioning its children.
            cinemaMap.post {
                val availableHeight = sheet.top.coerceIn(1, content.height.coerceAtLeast(1))
                if (cinemaMap.layoutParams.height != availableHeight) {
                    cinemaMap.updateLayoutParams { height = availableHeight }
                }
            }
        }

        fun sizeSheet() {
            if (content.height <= 0 || header.height <= 0) return
            // Leave a usable map above the expanded sheet, including in landscape.
            val sheetHeight = maxOf((content.height * 0.48f).toInt(), header.height + (48 * resources.displayMetrics.density).toInt())
                .coerceAtMost((content.height * 0.65f).toInt())
            if (sheet.layoutParams.height != sheetHeight) {
                sheet.updateLayoutParams { height = sheetHeight }
            }
            behavior.peekHeight = header.height.coerceAtMost(sheetHeight - 1)
        }

        header.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> sizeSheet() }
        content.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> sizeSheet() }
        sheet.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateMapHeight() }
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) = updateMapHeight()

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED ||
                    newState == BottomSheetBehavior.STATE_COLLAPSED
                ) {
                    toggle.setText(
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) R.string.collapse_sheet
                        else R.string.expand_sheet
                    )
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
        sheet.doOnLayout { sizeSheet() }
        findViewById<View>(R.id.reset_view).setOnClickListener { cinemaMap.resetView() }
        cinemaMap.onSeatClickListener = viewModel::onSeatTapped

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    cinemaMap.selectedSeatIds = state.selectedSeats.map { it.id }.toSet()
                    summary.text = getString(
                        R.string.selection_summary, state.totalSeatsCount, state.totalSpotsCount,
                        money.format(state.totalPriceVnd)
                    )
                    adapter.submitList(state.selectedSeats)
                    empty.isVisible = state.selectedSeats.isEmpty()
                    val seat = state.inspectedSeat
                    inspected.text = if (seat == null) {
                        getString(R.string.no_inspected_seat)
                    } else {
                        val status = when {
                            seat.id in cinemaMap.selectedSeatIds -> R.string.seat_selected
                            seat.availability == SeatAvailability.BOOKED -> R.string.seat_booked
                            seat.availability == SeatAvailability.UNAVAILABLE -> R.string.seat_unavailable
                            else -> R.string.seat_available
                        }
                        getString(
                            R.string.inspected_seat, seat.id,
                            getString(seat.typeLabelResource()), getString(status)
                        )
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
}
