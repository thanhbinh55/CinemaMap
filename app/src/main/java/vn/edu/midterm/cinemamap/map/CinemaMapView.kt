package vn.edu.midterm.cinemamap.map

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.animation.doOnEnd
import vn.edu.midterm.cinemamap.R
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatType

class CinemaMapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val geometry = MapGeometry(MockCinemaData.rows)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(24, 26, 42)
        textAlign = Paint.Align.CENTER
    }
    private val background = Color.rgb(247, 244, 237)
    private val screenColor = context.getColor(R.color.seat_accent)
    private val standardColor = context.getColor(R.color.seat_standard)
    private val vipColor = context.getColor(R.color.seat_vip)
    private val coupleColor = context.getColor(R.color.seat_couple)
    private val occupiedColor = context.getColor(R.color.seat_occupied)
    private val outlineColor = context.getColor(R.color.seat_outline)
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

    private var locatedSeatId: String? = null
    private var camera: CameraTransform? = null
    private var fitCamera: CameraTransform? = null
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
                val transform = camera ?: return false
                val point = transform.viewToWorld(event.x, event.y)
                val hit = geometry.findSeatAt(point)

                Log.d("CinemaMap", hit?.seat?.id ?: "Không trúng ghế")
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
                locatedSeatId = null
                invalidate()
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
        return if (mapSize <= viewportSize) {
            (viewportSize - mapSize) / 2f
        } else {
            offset.coerceIn(viewportSize - mapSize, 0f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(background)
        val transform = camera ?: return

        val checkpoint = canvas.save()
        canvas.translate(transform.offsetX, transform.offsetY)
        canvas.scale(transform.scale, transform.scale)
        drawScreen(canvas)

        var previousType: SeatType? = null
        for (row in geometry.rows) {
            if (previousType != row.type) {
                textPaint.textSize = 20f
                canvas.drawText(typeLabels.getValue(row.type), geometry.centerX, row.top - 5f, textPaint)
            }
            textPaint.textSize = 19f
            canvas.drawText(row.label, 17f, row.top + 23f, textPaint)
            for (position in row.seats) {
                val matching = matchingSeatIds?.contains(position.seat.id) != false
                val alpha = if (matching) 255 else 55
                drawSeat(canvas, position, alpha)
            }
            previousType = row.type
        }
        canvas.restoreToCount(checkpoint)
    }

    private fun drawScreen(canvas: Canvas) {
        paint.color = screenColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(geometry.centerX - 214f, 22f, geometry.centerX + 214f, 29f, paint)
        textPaint.textSize = 25f
        canvas.drawText(screenLabel, geometry.centerX, 58f, textPaint)
    }

    private fun drawSeat(canvas: Canvas, position: SeatPosition, alpha: Int) {
        val left = position.left
        val top = position.top
        val right = left + position.width
        val bottom = top + geometry.seatHeight
        val middleX = (left + right) / 2f
        val middleY = (top + bottom) / 2f
        val seat = position.seat
        val isSelected = seat.id in selectedSeatIds
        if (seat.id == locatedSeatId) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = screenColor
            canvas.drawCircle(middleX, middleY, position.width / 2f + 6f, paint)
        }

        // 1. Vẽ nền ghế
        paint.style = Paint.Style.FILL
        paint.color = if (seat.availability == SeatAvailability.AVAILABLE) {
            when (seat.type) {
                SeatType.STANDARD -> standardColor
                SeatType.VIP -> vipColor
                SeatType.COUPLE -> coupleColor
            }
        } else {
            occupiedColor
        }
        paint.alpha = alpha
        canvas.drawRect(left, top, right, bottom, paint)

        // 2. Vẽ viền và dấu trạng thái ĐANG CHỌN (nếu được chọn)
        paint.style = Paint.Style.STROKE
        if (isSelected) {
            paint.strokeWidth = 3f
            paint.color = screenColor // Màu chàm nổi bật
            paint.alpha = alpha
            canvas.drawRect(left + 1f, top + 1f, right - 1f, bottom - 1f, paint)

            // Vẽ dấu tick ✓
            paint.strokeWidth = 2.5f
            canvas.drawLine(middleX - 5f, middleY, middleX - 1f, middleY + 5f, paint)
            canvas.drawLine(middleX - 1f, middleY + 5f, middleX + 6f, middleY - 5f, paint)
        } else {
            paint.strokeWidth = 2f
            paint.color = outlineColor
            paint.alpha = alpha
            canvas.drawRect(left + 1f, top + 1f, right - 1f, bottom - 1f, paint)
        }

        // 3. Vạch chia giữa cho ghế đôi
        if (seat.type == SeatType.COUPLE && !isSelected) {
            paint.strokeWidth = 2f
            paint.color = outlineColor
            paint.alpha = alpha
            canvas.drawLine(middleX, top + 2f, middleX, bottom - 2f, paint)
        }

        // 4. Ký hiệu ghế đã đặt hoặc hỏng
        when (seat.availability) {
            SeatAvailability.BOOKED -> {
                paint.strokeWidth = 2f
                paint.color = outlineColor
                paint.alpha = alpha
                canvas.drawLine(middleX - 7f, middleY, middleX + 7f, middleY, paint)
            }
            SeatAvailability.UNAVAILABLE -> {
                paint.strokeWidth = 2f
                paint.color = outlineColor
                paint.alpha = alpha
                canvas.drawLine(middleX - 5f, middleY - 5f, middleX + 5f, middleY + 5f, paint)
                canvas.drawLine(middleX - 5f, middleY + 5f, middleX + 5f, middleY - 5f, paint)
            }
            SeatAvailability.AVAILABLE -> Unit
        }
        paint.alpha = 255
    }
}
