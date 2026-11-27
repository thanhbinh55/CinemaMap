package vn.edu.midterm.cinemamap.preview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import com.google.android.material.color.MaterialColors
import vn.edu.midterm.cinemamap.R
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Programmatic overlay with the same geometry as the 3D scene.
@SuppressLint("ViewConstructor")
internal class CinemaMiniMapView(
    context: Context,
    private val geometry: CinemaSceneGeometry,
    private val palette: ScenePalette,
    private val selectedIds: Set<String>,
    initialCamera: SeatCameraState,
    onOpenMap: () -> Unit
) : View(context) {
    private var camera = initialCamera
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val bounds = RectF()
    private val density = resources.displayMetrics.density
    private val surface = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface)
    private val foreground = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)

    init {
        setOnClickListener { onOpenMap() }
        updateCamera(initialCamera)
    }

    fun updateCamera(state: SeatCameraState) {
        camera = state
        contentDescription = context.getString(R.string.preview_minimap_description, state.seatId)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.style = Paint.Style.FILL
        paint.color = surface
        bounds.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(bounds, 10f * density, 10f * density, paint)
        paint.color = foreground
        paint.textSize = 11f * resources.displayMetrics.scaledDensity
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(context.getString(R.string.preview_minimap_title, camera.seatId), width / 2f, 16f * density, paint)
        val scale = minOf((width - 18f * density) / (geometry.halfWidth * 2f), (height - 31f * density) / geometry.depth)
        if (scale <= 0f) return
        val left = (width - geometry.halfWidth * 2f * scale) / 2f
        val top = 24f * density
        fun x(world: Float) = left + (world + geometry.halfWidth) * scale
        fun y(world: Float) = top + world * scale
        val saved = canvas.save()
        canvas.clipRect(left, top - 4f * density, width - left, height - 4f * density)
        paint.color = palette.selected
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * density
        path.reset()
        path.moveTo(x(-geometry.screenWidth / 2f), y(0f) + 2f * density)
        path.quadTo(x(0f), y(0f) - 2f * density, x(geometry.screenWidth / 2f), y(0f) + 2f * density)
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
        for (position in geometry.seats) {
            paint.color = when {
                position.seat.id in selectedIds -> palette.selected
                position.seat.availability == SeatAvailability.BOOKED -> palette.booked
                position.seat.availability == SeatAvailability.UNAVAILABLE -> palette.unavailable
                position.seat.type == SeatType.VIP -> palette.vip
                position.seat.type == SeatType.COUPLE -> palette.couple
                else -> palette.standard
            }
            canvas.drawRect(x(position.center.x - position.width / 2f), y(position.center.z - .25f),
                x(position.center.x + position.width / 2f), y(position.center.z + .25f), paint)
        }
        val eye = geometry.eyeAt(camera.seatId, camera.side)
        val direction = geometry.direction(camera)
        val length = sqrt(direction.x * direction.x + direction.z * direction.z).coerceAtLeast(0.001f)
        val dx = direction.x / length
        val dz = direction.z / length
        val angle = Math.toRadians(geometry.spec.horizontalFovDegrees / 2.0).toFloat()
        val reach = 4f
        path.reset()
        path.moveTo(x(eye.x), y(eye.z))
        path.lineTo(x(eye.x + (dx * cos(angle) - dz * sin(angle)) * reach), y(eye.z + (dx * sin(angle) + dz * cos(angle)) * reach))
        path.lineTo(x(eye.x + (dx * cos(angle) + dz * sin(angle)) * reach), y(eye.z + (-dx * sin(angle) + dz * cos(angle)) * reach))
        path.close()
        paint.color = palette.selected
        paint.alpha = 55
        canvas.drawPath(path, paint)
        paint.alpha = 255
        paint.strokeWidth = 2f * density
        canvas.drawLine(x(eye.x), y(eye.z), x(eye.x + dx * 2.7f), y(eye.z + dz * 2.7f), paint)
        canvas.drawCircle(x(eye.x), y(eye.z), 3.5f * density, paint)
        canvas.restoreToCount(saved)
    }
}
