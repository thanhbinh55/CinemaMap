package vn.edu.midterm.cinemamap.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
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
    private val screenColor = Color.rgb(60, 53, 147)
    private val standardColor = Color.rgb(225, 229, 247)
    private val vipColor = Color.rgb(247, 220, 183)
    private val coupleColor = Color.rgb(214, 239, 230)
    private val occupiedColor = Color.rgb(215, 215, 222)
    private val outlineColor = Color.rgb(81, 76, 97)
    private val screenLabel = resources.getString(R.string.screen_label)
    private val typeLabels = mapOf(
        SeatType.STANDARD to resources.getString(R.string.seat_standard),
        SeatType.VIP to resources.getString(R.string.seat_vip),
        SeatType.COUPLE to resources.getString(R.string.seat_couple)
    )

    private var fitScale = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitScale = minOf(w / geometry.width, h / geometry.height)
        offsetX = (w - geometry.width * fitScale) / 2f
        offsetY = (h - geometry.height * fitScale) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(background)
        if (fitScale <= 0f) return

        val checkpoint = canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(fitScale, fitScale)
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
                drawSeat(canvas, position)
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

    private fun drawSeat(canvas: Canvas, position: SeatPosition) {
        val left = position.left
        val top = position.top
        val right = left + position.width
        val bottom = top + geometry.seatHeight
        val middleX = (left + right) / 2f
        val middleY = (top + bottom) / 2f
        val seat = position.seat

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
        canvas.drawRect(left, top, right, bottom, paint)

        paint.style = Paint.Style.STROKE
        paint.color = outlineColor
        canvas.drawRect(left + 1f, top + 1f, right - 1f, bottom - 1f, paint)
        if (seat.type == SeatType.COUPLE) {
            canvas.drawLine(middleX, top + 2f, middleX, bottom - 2f, paint)
        }
        when (seat.availability) {
            SeatAvailability.BOOKED -> canvas.drawLine(
                middleX - 7f, middleY, middleX + 7f, middleY, paint
            )
            SeatAvailability.UNAVAILABLE -> {
                canvas.drawLine(middleX - 5f, middleY - 5f, middleX + 5f, middleY + 5f, paint)
                canvas.drawLine(middleX - 5f, middleY + 5f, middleX + 5f, middleY - 5f, paint)
            }
            SeatAvailability.AVAILABLE -> Unit
        }
    }
}
