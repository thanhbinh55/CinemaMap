package vn.edu.midterm.cinemamap.preview

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.color.MaterialColors
import vn.edu.midterm.cinemamap.R
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatType
import vn.edu.midterm.cinemamap.map.CinemaMapView
import vn.edu.midterm.cinemamap.ui.typeLabelResource

class CinemaPreviewActivity : AppCompatActivity() {
    private var sceneView: CinemaSceneView? = null
    private var displayedSeatId: String? = null
    private var displayedSide: SeatEye? = null
    private var miniMap: CinemaMiniMapView? = null
    private var trailer: CinemaTrailerPlayer? = null
    private var sceneFailed = false
    private lateinit var geometry: CinemaSceneGeometry
    private var roomSpec = CinemaRoomSpec()
    private var comparison = SeatComparison()
    private var fullscreen = false
    private val normalConstraints = ConstraintSet()
    private val selectedIds by lazy { intent.getStringArrayListExtra(EXTRA_SELECTED_IDS).orEmpty().toSet() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(0, 0), navigationBarStyle = SystemBarStyle.light(0, 0))
        setContentView(R.layout.activity_cinema_preview)
        val root = findViewById<ConstraintLayout>(R.id.preview_root)
        normalConstraints.clone(root)
        roomSpec = CinemaRoomSettings.load(this, savedInstanceState)
        geometry = CinemaSceneGeometry(MockCinemaData.rows, roomSpec)
        comparison = SeatComparison(restorePinned(savedInstanceState, "compare.a"), restorePinned(savedInstanceState, "compare.b"))
        fullscreen = savedInstanceState?.getBoolean(STATE_FULLSCREEN) ?: false
        val container = findViewById<FrameLayout>(R.id.preview_scene_container)
        val status = findViewById<TextView>(R.id.preview_status)
        val error = findViewById<TextView>(R.id.preview_error)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(if (fullscreen) WindowInsetsCompat.Type.displayCutout() else WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        findViewById<View>(R.id.close_3d_preview).setOnClickListener { finish() }
        findViewById<View>(R.id.reset_3d_look).setOnClickListener { sceneView?.resetLook() }
        findViewById<View>(R.id.change_preview_seat).setOnClickListener { showRowChooser() }
        val movieButton = findViewById<MaterialButton>(R.id.toggle_trailer)
        movieButton.setOnClickListener { trailer?.togglePlayback() }
        setupExtraControls()
        onBackPressedDispatcher.addCallback(this) {
            if (fullscreen) setFullscreen(false) else finish()
        }

        fun showError(message: Int) {
            sceneFailed = true
            trailer?.onPause()
            status.setText(message)
            error.setText(message)
            error.visibility = View.VISIBLE
            findViewById<View>(R.id.change_preview_seat).isEnabled = false
            findViewById<View>(R.id.reset_3d_look).isEnabled = false
            movieButton.isEnabled = false
            movieButton.setText(R.string.trailer_unavailable)
        }
        val id = savedInstanceState?.getString(STATE_SEAT) ?: intent.getStringExtra(EXTRA_SEAT_ID)
        if (id == null || geometry.seat(id) == null) {
            showError(R.string.preview_3d_invalid_seat)
            return
        }
        val initialSide = if (geometry.seat(id)?.seat?.type == SeatType.COUPLE)
            SeatEye.fromSavedValue(savedInstanceState?.getString(STATE_SIDE)) else SeatEye.LEFT
        val initialCamera = SeatCameraState(id, side = initialSide).lookBy(
            savedInstanceState?.getFloat(STATE_YAW) ?: 0f,
            savedInstanceState?.getFloat(STATE_PITCH) ?: 0f
        )
        updateSeatSummary(initialCamera)

        val manager = getSystemService(ActivityManager::class.java)
        if (manager.deviceConfigurationInfo.reqGlEsVersion < 0x20000) {
            showError(R.string.preview_3d_unsupported)
            return
        }

        trailer = CinemaTrailerPlayer(this,
            savedInstanceState?.getInt(STATE_TRAILER_POSITION) ?: 0,
            savedInstanceState?.getBoolean(STATE_TRAILER_PLAYING) ?: true
        ) { ready, playing, failed ->
            movieButton.isEnabled = ready && !failed && !sceneFailed
            movieButton.setText(when {
                failed -> R.string.trailer_unavailable
                !ready -> R.string.trailer_loading
                playing -> R.string.trailer_pause
                else -> R.string.trailer_play
            })
            movieButton.contentDescription = getString(if (playing) R.string.trailer_pause_description else R.string.trailer_play_description)
        }

        val palette = ScenePalette(getColor(R.color.seat_standard), getColor(R.color.seat_vip), getColor(R.color.seat_couple),
            getColor(R.color.seat_occupied), getColor(R.color.seat_unavailable), getColor(R.color.seat_accent))
        sceneView = CinemaSceneView(
            this, geometry,
            palette,
            selectedIds, initialCamera,
            onCameraChanged = ::updateSeatSummary,
            onVideoSurface = { trailer?.attachSurface(it) },
            onVideoError = { trailer?.fail(it) },
            onReady = {
                status.setText(R.string.preview_3d_ready)
                status.visibility = View.GONE
            },
            onError = { showError(R.string.preview_3d_error) }
        ).also { view ->
            view.id = R.id.preview_scene
            // Keep the error message above the SurfaceView in the View hierarchy.
            container.addView(view, 0, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            ))
        }
        miniMap = CinemaMiniMapView(this, geometry, palette, selectedIds, initialCamera, ::showMapPicker).also { view ->
            view.id = R.id.preview_minimap
            val density = resources.displayMetrics.density
            container.addView(view, FrameLayout.LayoutParams((128 * density).toInt(), (132 * density).toInt(),
                android.view.Gravity.TOP or android.view.Gravity.END).apply { setMargins((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), 0) })
        }
        refreshComparison()
        setFullscreen(fullscreen)
    }

    private fun setupExtraControls() {
        findViewById<View>(R.id.preview_options).setOnClickListener { anchor ->
            PopupMenu(this, anchor).apply {
                menu.add(0, 1, 0, R.string.preview_fullscreen)
                menu.add(0, 2, 1, R.string.room_settings_title)
                menu.add(0, 3, 2, R.string.preview_clear_comparison).isEnabled = comparison.a != null || comparison.b != null
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> setFullscreen(true)
                        2 -> CinemaRoomSettings.show(this@CinemaPreviewActivity, roomSpec) { spec ->
                            sceneView?.resetLook()
                            roomSpec = spec
                            CinemaRoomSettings.save(this@CinemaPreviewActivity, spec)
                            recreate()
                        }
                        3 -> { comparison = SeatComparison(); refreshComparison() }
                    }
                    true
                }
            }.show()
        }
        findViewById<View>(R.id.exit_preview_fullscreen).setOnClickListener { setFullscreen(false) }
        listOf(R.id.preview_compare_a, R.id.preview_compare_b).forEachIndexed { slot, id ->
            findViewById<MaterialButton>(id).apply {
                isCheckable = true
                setOnClickListener { compare(slot, false) }
                setOnLongClickListener { compare(slot, true); true }
            }
        }
        findViewById<MaterialButtonToggleGroup>(R.id.preview_seat_side).addOnButtonCheckedListener { _, id, checked ->
            if (checked) sceneView?.let { scene ->
                val side = when (id) {
                    R.id.preview_side_left -> SeatEye.LEFT
                    R.id.preview_side_right -> SeatEye.RIGHT
                    else -> return@addOnButtonCheckedListener
                }
                if (scene.cameraState.side != side) scene.showCamera(scene.cameraState.copy(side = side, yaw = 0f, pitch = 0f))
            }
        }
    }

    private fun compare(slot: Int, replace: Boolean) {
        val state = sceneView?.cameraState ?: return
        if (sceneFailed) return
        if (replace || comparison.target(slot) == null) comparison = comparison.pin(slot, state)
        trailer?.pauseForComparison()
        comparison.target(slot)?.let { sceneView?.showCamera(it) }
    }

    private fun refreshComparison() {
        listOf(R.id.preview_compare_a, R.id.preview_compare_b).forEachIndexed { slot, id ->
            val target = comparison.target(slot)
            val state = sceneView?.cameraState
            val isCouple = target != null && geometry.seat(target.seatId)?.seat?.type == SeatType.COUPLE
            findViewById<MaterialButton>(id).apply {
                text = if (target == null) getString(if (slot == 0) R.string.preview_pin_a else R.string.preview_pin_b)
                    else getString(R.string.preview_comparison_slot, if (slot == 0) "A" else "B",
                        target.seatId + if (isCouple) " (${getString(sideLabel(target.side))})" else "")
                contentDescription = when {
                    target == null -> text
                    isCouple -> getString(R.string.preview_comparison_description, target.seatId, getString(sideLabel(target.side)))
                    else -> getString(R.string.preview_comparison_seat_description, target.seatId)
                }
                isChecked = target != null && target.seatId == state?.seatId && target.side == state?.side
            }
        }
    }

    private fun sideLabel(side: SeatEye) = when (side) {
        SeatEye.LEFT -> R.string.preview_left
        SeatEye.RIGHT -> R.string.preview_right
    }

    private fun restorePinned(saved: Bundle?, prefix: String): SeatCameraState? {
        val id = saved?.getString("$prefix.id") ?: return null
        val seat = geometry.seat(id) ?: return null
        return SeatCameraState(id, side = if (seat.seat.type == SeatType.COUPLE)
            SeatEye.fromSavedValue(saved.getString("$prefix.side")) else SeatEye.LEFT)
    }

    private fun showMapPicker() {
        if (sceneFailed) return
        val map = CinemaMapView(this, null).apply {
            id = R.id.preview_map_picker
            selectedSeatIds = selectedIds
            sceneView?.cameraState?.seatId?.let(::highlightSeat)
        }
        val wrapper = FrameLayout(this).apply {
            addView(map, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                minOf(resources.displayMetrics.heightPixels / 2, (420 * resources.displayMetrics.density).toInt())))
        }
        lateinit var dialog: AlertDialog
        dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.preview_map_picker_title)
            .setMessage(R.string.preview_map_picker_hint)
            .setView(wrapper)
            .setNegativeButton(R.string.legend_close, null).create()
        map.onSeatClickListener = { seat -> sceneView?.showSeat(seat.id); dialog.dismiss() }
        dialog.show()
    }

    private fun setFullscreen(enabled: Boolean) {
        fullscreen = enabled
        val root = findViewById<ConstraintLayout>(R.id.preview_root)
        // The safe area beside a landscape cutout should blend into the scene.
        root.setBackgroundColor(if (enabled) getColor(R.color.preview_3d_background)
            else MaterialColors.getColor(root, com.google.android.material.R.attr.colorSurface))
        val constraints = ConstraintSet().apply { clone(normalConstraints) }
        if (enabled) {
            val panel = if (findViewById<View>(R.id.preview_info) != null) R.id.preview_info else R.id.preview_controls_scroll
            constraints.setVisibility(panel, View.GONE)
            constraints.setVisibility(R.id.preview_toolbar, View.GONE)
            constraints.clear(R.id.preview_scene_container, ConstraintSet.TOP)
            constraints.clear(R.id.preview_scene_container, ConstraintSet.BOTTOM)
            constraints.clear(R.id.preview_scene_container, ConstraintSet.END)
            constraints.connect(R.id.preview_scene_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraints.connect(R.id.preview_scene_container, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constraints.connect(R.id.preview_scene_container, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        }
        constraints.applyTo(root)
        findViewById<View>(R.id.exit_preview_fullscreen).isVisible = enabled
        WindowCompat.getInsetsController(window, root).apply {
            isAppearanceLightStatusBars = !enabled
            isAppearanceLightNavigationBars = !enabled
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (enabled) hide(WindowInsetsCompat.Type.systemBars()) else show(WindowInsetsCompat.Type.systemBars())
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun statusLabel(seat: Seat): Int = when {
        seat.id in selectedIds -> R.string.seat_selected
        seat.availability == SeatAvailability.BOOKED -> R.string.seat_booked
        seat.availability == SeatAvailability.UNAVAILABLE -> R.string.seat_unavailable
        else -> R.string.seat_available
    }

    private fun updateSeatSummary(state: SeatCameraState) {
        if (displayedSeatId != state.seatId || displayedSide != state.side) {
            val seat = requireNotNull(geometry.seat(state.seatId)).seat
            findViewById<TextView>(R.id.preview_title).text = getString(R.string.preview_3d_title, seat.id)
            val detail = getString(R.string.preview_3d_seat_details,
                seat.id, getString(seat.typeLabelResource()), seat.type.capacity, getString(statusLabel(seat)))
            findViewById<TextView>(R.id.preview_seat_details).text = if (seat.type == SeatType.COUPLE)
                getString(R.string.preview_couple_detail, detail, getString(sideLabel(state.side))) else detail
            displayedSeatId = seat.id
            displayedSide = state.side
            // Both the toolbar and system Back return the last seat viewed, not a booking change.
            setResult(RESULT_OK, Intent().putExtra(EXTRA_SEAT_ID, seat.id))
        }
        findViewById<View>(R.id.reset_3d_look).isEnabled = state.yaw != 0f || state.pitch != 0f
        val sides = findViewById<MaterialButtonToggleGroup>(R.id.preview_seat_side)
        sides.isVisible = geometry.seat(state.seatId)?.seat?.type == SeatType.COUPLE
        sides.check(when (state.side) {
            SeatEye.LEFT -> R.id.preview_side_left
            SeatEye.RIGHT -> R.id.preview_side_right
        })
        miniMap?.updateCamera(state)
        refreshComparison()
    }

    private fun showRowChooser() {
        val groups = geometry.seats.groupBy { it.row }.entries.toList()
        val labels = groups.map { getString(R.string.preview_3d_row, it.key.toString(),
            getString(it.value.first().seat.typeLabelResource())) }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.preview_3d_choose_row)
            .setItems(labels) { _, index ->
                val row = groups[index]
                val seats = row.value.map { it.seat }
                val seatLabels = seats.map { getString(R.string.preview_3d_seat_option, it.id, getString(statusLabel(it))) }.toTypedArray()
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.preview_3d_choose_seat, row.key.toString()))
                    .setSingleChoiceItems(seatLabels, seats.indexOfFirst { it.id == sceneView?.cameraState?.seatId }) { dialog, seatIndex ->
                        sceneView?.showSeat(seats[seatIndex].id)
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.legend_close, null)
                    .show()
            }
            .setNegativeButton(R.string.legend_close, null)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        CinemaRoomSettings.storeState(outState, roomSpec)
        outState.putBoolean(STATE_FULLSCREEN, fullscreen)
        listOf(comparison.a, comparison.b).forEachIndexed { slot, state ->
            if (state != null) {
                val prefix = if (slot == 0) "compare.a" else "compare.b"
                outState.putString("$prefix.id", state.seatId)
                outState.putString("$prefix.side", state.side.name)
            }
        }
        trailer?.let {
            outState.putInt(STATE_TRAILER_POSITION, it.positionMs())
            outState.putBoolean(STATE_TRAILER_PLAYING, it.playRequested)
        }
        sceneView?.cameraState?.let {
            outState.putString(STATE_SEAT, it.seatId)
            outState.putFloat(STATE_YAW, it.yaw)
            outState.putFloat(STATE_PITCH, it.pitch)
            outState.putString(STATE_SIDE, it.side.name)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        trailer?.onResume()
        sceneView?.onResume()
        if (fullscreen) setFullscreen(true)
    }

    override fun onPause() {
        trailer?.onPause()
        sceneView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        trailer?.release()
        trailer = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_SEAT_ID = "preview.seat_id"
        const val EXTRA_SELECTED_IDS = "preview.selected_ids"
        private const val STATE_SEAT = "preview.camera_seat"
        private const val STATE_YAW = "preview.camera_yaw"
        private const val STATE_PITCH = "preview.camera_pitch"
        private const val STATE_SIDE = "preview.camera_side"
        private const val STATE_FULLSCREEN = "preview.fullscreen"
        private const val STATE_TRAILER_POSITION = "preview.trailer_position"
        private const val STATE_TRAILER_PLAYING = "preview.trailer_playing"
    }
}
