package vn.edu.midterm.cinemamap.preview

import vn.edu.midterm.cinemamap.map.MapGeometry
import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatRow
import vn.edu.midterm.cinemamap.model.SeatType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal data class Point3(val x: Float, val y: Float, val z: Float)

internal data class SceneSeat(
    val seat: Seat,
    val row: Char,
    val center: Point3,
    val width: Float
)

internal data class SceneRow(val floorY: Float, val frontZ: Float, val backZ: Float)

/** Metres are illustrative; IDs, horizontal positions and row gaps come from the 2D layout. */
internal class CinemaSceneGeometry(seatRows: List<SeatRow>, val spec: CinemaRoomSpec = CinemaRoomSpec()) {
    val screenCenter = Point3(0f, spec.screenCenterY, 0.12f)
    val screenWidth = spec.screenWidth
    val screenHeight = spec.screenHeight
    val eyeHeight = spec.eyeHeight
    val halfWidth: Float
    val depth: Float
    val seats: List<SceneSeat>
    val rows: List<SceneRow>
    private val seatsById: Map<String, SceneSeat>

    init {
        val map = MapGeometry(seatRows)
        val origin = map.rows.first().top
        val xScale = (spec.roomWidth - 2f) / map.width
        halfWidth = spec.roomWidth / 2f
        val centers = map.rows.map { spec.firstRowDistance + (it.top - origin) / 40f * spec.rowSpacing }
        rows = map.rows.mapIndexed { index, _ ->
            SceneRow(
                floorY = 0.08f + index * spec.rowRise,
                frontZ = if (index == 0) centers[index] - 0.56f else (centers[index - 1] + centers[index]) / 2f,
                backZ = if (index == centers.lastIndex) centers[index] + 0.7f else (centers[index] + centers[index + 1]) / 2f
            )
        }
        depth = rows.last().backZ + 1.5f
        seats = map.rows.flatMapIndexed { index, row ->
            row.seats.map { position ->
                SceneSeat(
                    position.seat, row.label.single(),
                    Point3((position.left + position.width / 2f - map.centerX) * xScale,
                        rows[index].floorY, centers[index]),
                    position.width * xScale
                )
            }
        }
        seatsById = seats.associateBy { it.seat.id }
    }

    fun seat(id: String): SceneSeat? = seatsById[id]

    fun eyeAt(id: String, side: SeatEye = SeatEye.LEFT): Point3 {
        val seat = requireNotNull(seat(id)) { "Unknown seat: $id" }
        val offset = if (seat.seat.type != SeatType.COUPLE) 0f else when (side) {
            SeatEye.LEFT -> -seat.width / 4f
            SeatEye.RIGHT -> seat.width / 4f
        }
        return seat.center.copy(x = seat.center.x + offset, y = seat.center.y + eyeHeight)
    }

    fun direction(state: SeatCameraState): Point3 {
        val eye = eyeAt(state.seatId, state.side)
        val dx = screenCenter.x - eye.x
        val dy = screenCenter.y - eye.y
        val dz = screenCenter.z - eye.z
        val yaw = atan2(dx, -dz) + Math.toRadians(state.yaw.toDouble()).toFloat()
        val basePitch = atan2(dy, sqrt(dx * dx + dz * dz))
        val pitch = (basePitch + Math.toRadians(state.pitch.toDouble()).toFloat())
            .coerceIn(Math.toRadians(-75.0).toFloat(), Math.toRadians(75.0).toFloat())
        return Point3(sin(yaw) * cos(pitch), sin(pitch), -cos(yaw) * cos(pitch))
    }
}

// Side is meaningful for a couple seat; single seats always use their own centre.
internal data class SeatCameraState(val seatId: String, val yaw: Float = 0f, val pitch: Float = 0f, val side: SeatEye = SeatEye.LEFT) {
    // Wrap horizontal turns at +/-180 degrees; limit vertical tilt to avoid flipping.
    fun lookBy(yawDelta: Float, pitchDelta: Float): SeatCameraState = copy(
        yaw = (yaw + yawDelta).let { if (it.isFinite()) ((it + 180f) % 360f + 360f) % 360f - 180f else 0f },
        pitch = (pitch + pitchDelta).let { if (it.isFinite()) it.coerceIn(-45f, 45f) else 0f }
    )
}
