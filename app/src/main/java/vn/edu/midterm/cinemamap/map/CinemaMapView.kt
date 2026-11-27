package vn.edu.midterm.cinemamap.map

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.os.BundleCompat
import vn.edu.midterm.cinemamap.R
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatType

class CinemaMapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val geometry = MapGeometry(MockCinemaData.rows)
    private val screenPath = Path().apply {
        moveTo(geometry.centerX - 214f, 32f)
        quadTo(geometry.centerX, 6f, geometry.centerX + 214f, 32f)
    }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.app_text)
        textAlign = Paint.Align.CENTER
    }
    private val background = context.getColor(R.color.app_background)
    private val screenColor = context.getColor(R.color.app_navy)
    private val selectedColor = context.getColor(R.color.seat_accent)
    private val standardLabelColor = context.getColor(R.color.seat_standard_text)
    private val vipLabelColor = context.getColor(R.color.seat_vip_text)
    private val coupleLabelColor = context.getColor(R.color.seat_couple_text)
    private val occupiedLabelColor = context.getColor(R.color.seat_occupied_text)
    private val unavailableLabelColor = context.getColor(R.color.seat_unavailable_text)
    private val miniBackground = context.getColor(R.color.app_background)
    private val miniOutline = context.getColor(R.color.app_divider)
    private val miniViewportColor = context.getColor(R.color.map_viewport)
    private val standardColor = context.getColor(R.color.seat_standard)
    private val vipColor = context.getColor(R.color.seat_vip)
    private val coupleColor = context.getColor(R.color.seat_couple)
    private val unavailableColor = context.getColor(R.color.seat_unavailable)
    private val occupiedColor = context.getColor(R.color.seat_occupied)
    private val screenLabel = resources.getString(R.string.screen_label)
    private val typeLabels = mapOf(
        SeatType.STANDARD to resources.getString(R.string.seat_standard),
        SeatType.VIP to resources.getString(R.string.seat_vip),
        SeatType.COUPLE to resources.getString(R.string.seat_couple)
    )

    var onSeatClickListener: ((vn.edu.midterm.cinemamap.model.Seat) -> Unit)? = null

    var selectedSeatIds: Set<String> = emptySet()
        set(value) {
            if (field != value) {
                field = value
                if (locatedSeatId !in value) locatedSeatId = null
                invalidate()
            }
        }
    var matchingSeatIds: Set<String>? = null
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private val miniBounds = RectF()
    private val miniViewport = RectF()
    private val miniPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val seatLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private data class SeatLabelMetrics(val textSize: Float, val baselineOffset: Float)

    private val seatLabels = geometry.rows.flatMap { it.seats }.associate { position ->
        seatLabelPaint.textSize = 11.2f
        val labelWidth = seatLabelPaint.measureText(position.seat.id)
        val availableWidth = position.width - 8f
        if (labelWidth > availableWidth) seatLabelPaint.textSize *= availableWidth / labelWidth
        position.seat.id to SeatLabelMetrics(seatLabelPaint.textSize,
            -(seatLabelPaint.ascent() + seatLabelPaint.descent()) / 2f)
    }
    private var locatedSeatId: String? = null
    private var camera: CameraTransform? = null
    private var fitCamera: CameraTransform? = null
    private var pendingCameraState: FloatArray? = null
    private var resetAnimator: ValueAnimator? = null
    private var previousFocusX = 0f
    private var previousFocusY = 0f
    private var hadMultiplePointers = false
    private var remainingPointerId: Int? = null
    private var remainingPointerX = 0f
    private var remainingPointerY = 0f
    private val tapDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean = true

            override fun onScroll(
                firstEvent: MotionEvent?,
                currentEvent: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (hadMultiplePointers) return false
                return panBy(-distanceX, -distanceY)
            }

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                if (hadMultiplePointers) return false
                if (miniBounds.contains(event.x, event.y)) {
                    resetView()
                    performClick()
                    return true
                }
                val transform = camera ?: return false
                val point = transform.viewToWorld(event.x, event.y)
                val hit = geometry.findSeatAt(point)

                if (hit != null && matchingSeatIds?.contains(hit.seat.id) != false) {
                    onSeatClickListener?.invoke(hit.seat)
                }
                performClick()
                return true
            }
        }
    )

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                previousFocusX = detector.focusX
                previousFocusY = detector.focusY
                return camera != null
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val current = camera ?: return false
                val fitScale = fitCamera?.scale ?: return false
                val anchor = current.viewToWorld(previousFocusX, previousFocusY)

                val newScale = (current.scale * detector.scaleFactor)
                    .coerceIn(fitScale * 0.8f, fitScale * 5f)

                val newOffsetX = detector.focusX - anchor.x * newScale
                val newOffsetY = detector.focusY - anchor.y * newScale

                camera = boundedCamera(newScale, newOffsetX, newOffsetY)

                previousFocusX = detector.focusX
                previousFocusY = detector.focusY
                invalidate()
                return true
            }
        }
    ).apply {
        isQuickScaleEnabled = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                resetAnimator?.cancel()
                if (locatedSeatId != null) {
                    locatedSeatId = null
                    invalidate()
                }
                hadMultiplePointers = false
                remainingPointerId = null
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                hadMultiplePointers = true
                remainingPointerId = null
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount == 2) {
                    val remainingIndex = 1 - event.actionIndex
                    remainingPointerId = event.getPointerId(remainingIndex)
                    remainingPointerX = event.getX(remainingIndex)
                    remainingPointerY = event.getY(remainingIndex)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && event.getPointerId(0) == remainingPointerId) {
                    val x = event.x
                    val y = event.y
                    panBy(x - remainingPointerX, y - remainingPointerY)
                    remainingPointerX = x
                    remainingPointerY = y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> remainingPointerId = null
        }

        scaleDetector.onTouchEvent(event)
        val handledTap = tapDetector.onTouchEvent(event)
        return handledTap || hadMultiplePointers || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetAnimator?.cancel()
        val oldCamera = camera
        val oldFit = fitCamera
        val oldCenter = oldCamera?.viewToWorld(oldw / 2f, oldh / 2f)
        val scale = minOf(w / geometry.width, h / geometry.height)
        val offsetX = (w - geometry.width * scale) / 2f
        val offsetY = (h - geometry.height * scale) / 2f

        fitCamera = if (scale > 0f) CameraTransform(scale, offsetX, offsetY) else null
        camera = if (oldCamera != null && oldFit != null && oldCenter != null && oldCamera != oldFit && scale > 0f) {
            val resizedScale = scale * (oldCamera.scale / oldFit.scale)
            boundedCamera(resizedScale, w / 2f - oldCenter.x * resizedScale, h / 2f - oldCenter.y * resizedScale)
        } else {
            fitCamera
        }
        restoreCameraWhenReady()
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable("super", super.onSaveInstanceState())
        val current = camera
        val fit = fitCamera
        val snapshot = if (current != null && fit != null && fit.scale > 0f) {
            val center = current.viewToWorld(width / 2f, height / 2f)
            floatArrayOf(current.scale / fit.scale, center.x, center.y, if (current == fit) 1f else 0f)
        } else pendingCameraState
        putFloatArray("camera", snapshot)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(BundleCompat.getParcelable(state, "super", Parcelable::class.java))
        pendingCameraState = state.getFloatArray("camera")?.takeIf {
            it.size == 4 && it.all(Float::isFinite) && it[0] > 0f
        }
        restoreCameraWhenReady()
    }

    private fun restoreCameraWhenReady() {
        val snapshot = pendingCameraState ?: return
        val fit = fitCamera ?: return
        if (width <= 0 || height <= 0) return
        camera = if (snapshot[3] == 1f) fit else {
            val scale = fit.scale * snapshot[0].coerceIn(0.8f, 5f)
            boundedCamera(scale, width / 2f - snapshot[1] * scale, height / 2f - snapshot[2] * scale)
        }
        pendingCameraState = null
        invalidate()
    }

    override fun onDetachedFromWindow() {
        resetAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    fun locateSeat(seatId: String) {
        val position = geometry.rows.asSequence().flatMap { it.seats.asSequence() }
            .firstOrNull { it.seat.id == seatId } ?: return
        val fit = fitCamera ?: return
        resetAnimator?.cancel()
        val scale = maxOf(camera?.scale ?: fit.scale, fit.scale * 2.5f)
        camera = boundedCamera(
            scale,
            width / 2f - (position.left + position.width / 2f) * scale,
            height / 2f - (position.top + geometry.seatHeight / 2f) * scale
        )
        locatedSeatId = seatId
        invalidate()
    }

    fun resetView() {
        val current = camera ?: return
        val target = fitCamera ?: return
        resetAnimator?.cancel()
        if (current == target) return

        if (!ValueAnimator.areAnimatorsEnabled()) {
            camera = target
            invalidate()
            return
        }

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 220L
            addUpdateListener { animation ->
                val progress = animation.animatedFraction
                camera = if (progress == 1f) {
                    target
                } else {
                    boundedCamera(
                        current.scale + (target.scale - current.scale) * progress,
                        current.offsetX + (target.offsetX - current.offsetX) * progress,
                        current.offsetY + (target.offsetY - current.offsetY) * progress
                    )
                }
                invalidate()
            }
            doOnEnd { resetAnimator = null }
        }
        resetAnimator = animator
        animator.start()
    }

    private fun panBy(deltaX: Float, deltaY: Float): Boolean {
        val current = camera ?: return false
        val next = boundedCamera(current.scale, current.offsetX + deltaX, current.offsetY + deltaY)
        if (next != current) {
            camera = next
            invalidate()
        }
        return true
    }

    private fun boundedCamera(scale: Float, offsetX: Float, offsetY: Float): CameraTransform {
        return CameraTransform(
            scale,
            limitOffset(offsetX, width.toFloat(), geometry.width * scale),
            limitOffset(offsetY, height.toFloat(), geometry.height * scale)
        )
    }

    private fun limitOffset(offset: Float, viewportSize: Float, mapSize: Float): Float {
        val margin = viewportSize * 0.15f
        return if (mapSize <= viewportSize) {
            val centered = (viewportSize - mapSize) / 2f
            offset.coerceIn(centered - margin, centered + margin)
        } else {
            offset.coerceIn(viewportSize - mapSize - margin, margin)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(background)
        val transform = camera ?: return

        val checkpoint = canvas.save()
        canvas.translate(transform.offsetX, transform.offsetY)
        canvas.scale(transform.scale, transform.scale)
        drawRoom(canvas, transform)
        canvas.restoreToCount(checkpoint)
        drawMiniMap(canvas, transform)
    }

    private fun drawRoom(canvas: Canvas, transform: CameraTransform) {
        drawScreen(canvas)
        val visibleLeft = -transform.offsetX / transform.scale
        val visibleTop = -transform.offsetY / transform.scale
        val visibleRight = (width - transform.offsetX) / transform.scale
        val visibleBottom = (height - transform.offsetY) / transform.scale
        var previousType: SeatType? = null
        for (row in geometry.rows) {
            if (previousType != row.type) {
                textPaint.textSize = 20f
                canvas.drawText(typeLabels.getValue(row.type), geometry.centerX, row.top - 5f, textPaint)
            }
            textPaint.textSize = 19f
            canvas.drawText(row.label, 17f, row.top + 23f, textPaint)
            for (position in row.seats) {
                if (position.seat.id != locatedSeatId && (position.left > visibleRight ||
                    position.left + position.width < visibleLeft || position.top > visibleBottom ||
                    position.top + geometry.seatHeight < visibleTop)) continue
                val matching = matchingSeatIds?.contains(position.seat.id) != false
                drawSeat(canvas, position, if (matching) 255 else 55)
            }
            previousType = row.type
        }
    }

    private fun drawScreen(canvas: Canvas) {
        paint.color = screenColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawPath(screenPath, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.style = Paint.Style.FILL
        textPaint.textSize = 25f
        canvas.drawText(screenLabel, geometry.centerX, 58f, textPaint)
    }

    private fun seatColor(position: SeatPosition): Int {
        val seat = position.seat
        return when {
            seat.id in selectedSeatIds -> selectedColor
            seat.availability == SeatAvailability.BOOKED -> occupiedColor
            seat.availability == SeatAvailability.UNAVAILABLE -> unavailableColor
            seat.type == SeatType.VIP -> vipColor
            seat.type == SeatType.COUPLE -> coupleColor
            else -> standardColor
        }
    }

    private fun drawSeat(canvas: Canvas, position: SeatPosition, alpha: Int) {
        val left = position.left
        val top = position.top
        val right = left + position.width
        val bottom = top + geometry.seatHeight
        val middleX = (left + right) / 2f
        val middleY = (top + bottom) / 2f
        val seat = position.seat
        val selected = seat.id in selectedSeatIds

        paint.style = Paint.Style.FILL
        paint.color = seatColor(position)
        paint.alpha = alpha
        canvas.drawRoundRect(left, top, right, bottom, 4f, 4f, paint)
        if (seat.id == locatedSeatId) {
            paint.style = Paint.Style.STROKE
            paint.color = selectedColor
            paint.strokeWidth = 3f
            canvas.drawCircle(middleX, middleY, position.width / 2f + 6f, paint)
        }

        seatLabelPaint.color = when {
            selected -> Color.WHITE
            seat.availability == SeatAvailability.BOOKED -> occupiedLabelColor
            seat.availability == SeatAvailability.UNAVAILABLE -> unavailableLabelColor
            seat.type == SeatType.VIP -> vipLabelColor
            seat.type == SeatType.COUPLE -> coupleLabelColor
            else -> standardLabelColor
        }
        seatLabelPaint.alpha = alpha
        val label = seatLabels.getValue(seat.id)
        seatLabelPaint.textSize = label.textSize
        canvas.drawText(seat.id, middleX, middleY + label.baselineOffset, seatLabelPaint)
        paint.alpha = 255
    }

    private fun drawMiniMap(canvas: Canvas, transform: CameraTransform) {
        val fit = fitCamera ?: return
        val moved = kotlin.math.abs(transform.offsetX - fit.offsetX) > 1f ||
            kotlin.math.abs(transform.offsetY - fit.offsetY) > 1f
        if (transform.scale <= fit.scale * 1.05f && !moved) {
            miniBounds.setEmpty()
            return
        }
        val density = resources.displayMetrics.density
        val miniWidth = minOf(112f * density, width * 0.25f)
        val miniScale = (miniWidth - 12f * density) / geometry.width
        if (miniScale <= 0f) return
        val miniHeight = geometry.height * miniScale + 12f * density
        val margin = 8f * density
        miniBounds.set(width - margin - miniWidth, margin, width - margin, margin + miniHeight)
        miniPaint.style = Paint.Style.FILL
        miniPaint.color = miniBackground
        canvas.drawRoundRect(miniBounds, 8f * density, 8f * density, miniPaint)
        miniPaint.style = Paint.Style.STROKE
        miniPaint.color = miniOutline
        miniPaint.strokeWidth = density
        canvas.drawRoundRect(miniBounds, 8f * density, 8f * density, miniPaint)
        val originX = miniBounds.left + 6f * density
        val originY = miniBounds.top + 6f * density
        val saved = canvas.save()
        canvas.translate(originX, originY)
        canvas.scale(miniScale, miniScale)
        canvas.clipRect(0f, 0f, geometry.width, geometry.height)
        miniPaint.strokeWidth = 7f
        miniPaint.color = screenColor
        miniPaint.style = Paint.Style.STROKE
        canvas.drawPath(screenPath, miniPaint)
        for (row in geometry.rows) {
            for (position in row.seats) {
                miniPaint.style = Paint.Style.FILL
                miniPaint.color = seatColor(position)
                miniPaint.alpha = if (matchingSeatIds?.contains(position.seat.id) != false) 255 else 65
                canvas.drawRect(position.left, position.top, position.left + position.width,
                    position.top + geometry.seatHeight, miniPaint)
            }
        }
        val topLeft = transform.viewToWorld(0f, 0f)
        val bottomRight = transform.viewToWorld(width.toFloat(), height.toFloat())
        miniViewport.set(
            topLeft.x.coerceIn(0f, geometry.width), topLeft.y.coerceIn(0f, geometry.height),
            bottomRight.x.coerceIn(0f, geometry.width), bottomRight.y.coerceIn(0f, geometry.height)
        )
        miniPaint.alpha = 255
        miniPaint.style = Paint.Style.STROKE
        miniPaint.color = miniViewportColor
        miniPaint.strokeWidth = 2f * density / miniScale
        canvas.drawRect(miniViewport, miniPaint)
        canvas.restoreToCount(saved)
    }

    fun highlightSeat(seatId: String) {
        if (geometry.rows.any { row -> row.seats.any { it.seat.id == seatId } }) {
            locatedSeatId = seatId
            invalidate()
        }
    }
}
