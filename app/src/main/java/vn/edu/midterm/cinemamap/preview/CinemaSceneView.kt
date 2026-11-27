package vn.edu.midterm.cinemamap.preview

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.Surface
import vn.edu.midterm.cinemamap.R

// Created programmatically with geometry/camera dependencies, never inflated from XML.
@SuppressLint("ViewConstructor")
internal class CinemaSceneView(
    context: Context,
    private val geometry: CinemaSceneGeometry,
    palette: ScenePalette,
    selectedIds: Set<String>,
    initialCamera: SeatCameraState,
    private val onCameraChanged: (SeatCameraState) -> Unit,
    onVideoSurface: (Surface) -> Unit,
    onVideoError: (Exception) -> Unit,
    onReady: () -> Unit,
    onError: () -> Unit
) : GLSurfaceView(context) {
    var cameraState = initialCamera
        private set
    private val sceneRenderer = CinemaSceneRenderer(
        geometry, CinemaSceneMesh(geometry, palette, selectedIds), initialCamera,
        requestFrame = { requestRender() },
        onVideoSurface = { output -> post { onVideoSurface(output) } },
        onVideoError = { error -> post { onVideoError(error) } },
        onReady = { post { onReady() } }, onError = { post { onError() } }
    )
    private var blockedGesture = false
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent) = true

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val degreesPerPixel = 0.22f / resources.displayMetrics.density
            setCamera(cameraState.lookBy(-distanceX * degreesPerPixel, distanceY * degreesPerPixel))
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean = performClick()
    })

    init {
        updateDescription()
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 0, 16, 0)
        preserveEGLContextOnPause = true
        setRenderer(sceneRenderer)
        // Redraw on camera/resize/video frames; no independent animation loop.
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onResume() {
        queueEvent { sceneRenderer.requestVideoSurface() }
        super.onResume()
        requestRender()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow() // Joins the GL thread before releasing Java resources.
        sceneRenderer.releaseVideoSurface()
    }

    fun showSeat(id: String) {
        requireNotNull(geometry.seat(id))
        setCamera(SeatCameraState(id))
    }

    fun showCamera(state: SeatCameraState) {
        val seat = requireNotNull(geometry.seat(state.seatId))
        setCamera(state.copy(side = if (seat.seat.type == vn.edu.midterm.cinemamap.model.SeatType.COUPLE) state.side else SeatEye.LEFT))
    }

    fun resetLook() = setCamera(cameraState.copy(yaw = 0f, pitch = 0f))

    private fun setCamera(state: SeatCameraState) {
        val changedSeat = cameraState.seatId != state.seatId
        cameraState = state
        queueEvent { sceneRenderer.updateCamera(state) }
        requestRender()
        if (changedSeat) updateDescription()
        onCameraChanged(state)
    }

    private fun updateDescription() {
        contentDescription = context.getString(R.string.preview_3d_accessibility, cameraState.seatId)
    }

    // Clicks are dispatched through GestureDetector.onSingleTapUp -> performClick().
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            blockedGesture = false
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        if (event.pointerCount > 1 && !blockedGesture) {
            blockedGesture = true
            val cancel = MotionEvent.obtain(event).apply { action = MotionEvent.ACTION_CANCEL }
            gestureDetector.onTouchEvent(cancel)
            cancel.recycle()
        }
        if (!blockedGesture) gestureDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            parent?.requestDisallowInterceptTouchEvent(false)
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
