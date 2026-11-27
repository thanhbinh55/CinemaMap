package vn.edu.midterm.cinemamap.preview

import org.junit.Assert.*
import org.junit.Test
import vn.edu.midterm.cinemamap.data.MockCinemaData
import kotlin.math.sqrt

class CinemaSceneGeometryTest {
    private val geometry = CinemaSceneGeometry(MockCinemaData.rows)

    @Test
    fun sameIdsAndCentralAisleAsTheBookingLayout() {
        assertEquals(MockCinemaData.rows.flatMap { it.seats }.map { it.id }.toSet(), geometry.seats.map { it.seat.id }.toSet())
        assertEquals(196, geometry.seats.size)
        for (row in MockCinemaData.rows) {
            val left = geometry.seat(row.seats.first().id)!!
            val right = geometry.seat(row.seats.last().id)!!
            assertEquals(-left.center.x, right.center.x, 0.0001f)
            val aisleLeft = geometry.seat(row.seats[row.seats.size / 2 - 1].id)!!
            val aisleRight = geometry.seat(row.seats[row.seats.size / 2].id)!!
            assertEquals(0.88f, (aisleRight.center.x - aisleRight.width / 2) -
                (aisleLeft.center.x + aisleLeft.width / 2), 0.0001f)
        }
    }

    @Test
    fun rowDepthAndElevationIncreaseAndCouplesStayWide() {
        for ((a, b) in geometry.rows.zipWithNext()) {
            assertEquals(a.backZ, b.frontZ, 0.0001f)
            assertTrue(b.floorY > a.floorY)
        }
        assertTrue(geometry.eyeAt("L04").z > geometry.eyeAt("A09").z)
        assertTrue(geometry.eyeAt("L04").y > geometry.eyeAt("A09").y)
        assertTrue(geometry.seat("K04")!!.width > geometry.seat("A09")!!.width * 2)
        assertEquals(geometry.eyeHeight, geometry.eyeAt("F08").y - geometry.seat("F08")!!.center.y, 0.0001f)
        assertNull(geometry.seat("Z99"))
    }

    @Test
    fun everyDefaultCameraPointsToScreenCenterFromItsOwnSeat() {
        for (seat in geometry.seats) {
            val eye = geometry.eyeAt(seat.seat.id)
            val dx = geometry.screenCenter.x - eye.x
            val dy = geometry.screenCenter.y - eye.y
            val dz = geometry.screenCenter.z - eye.z
            val length = sqrt(dx * dx + dy * dy + dz * dz)
            val direction = geometry.direction(SeatCameraState(seat.seat.id))
            assertEquals(dx / length, direction.x, 0.0001f)
            assertEquals(dy / length, direction.y, 0.0001f)
            assertEquals(dz / length, direction.z, 0.0001f)
        }
    }

    @Test
    fun lookChangesDirectionWithoutMovingEyeAndStaysFiniteAtLimits() {
        val initial = SeatCameraState("F08")
        val moved = initial.lookBy(30f, -20f)
        assertEquals(geometry.eyeAt(initial.seatId), geometry.eyeAt(moved.seatId))
        assertNotEquals(geometry.direction(initial), geometry.direction(moved))
        val limited = moved.lookBy(10_000f, -10_000f)
        assertTrue(limited.yaw >= -180f && limited.yaw < 180f)
        assertEquals(-45f, limited.pitch, 0f)
        assertEquals(initial, initial.lookBy(720f, 0f))
        val direction = geometry.direction(limited)
        assertTrue(direction.x.isFinite() && direction.y.isFinite() && direction.z.isFinite())
        assertEquals(1f, sqrt(direction.x * direction.x + direction.y * direction.y + direction.z * direction.z), 0.0001f)
        assertEquals(initial, initial.lookBy(Float.NaN, Float.POSITIVE_INFINITY))
    }

    @Test
    fun coupleEyesAreSymmetricAndEachFacesTheScreenCenter() {
        val seat = geometry.seat("L04")!!
        val center = seat.center.copy(y = seat.center.y + geometry.eyeHeight)
        val left = geometry.eyeAt("L04", SeatEye.LEFT)
        val right = geometry.eyeAt("L04", SeatEye.RIGHT)
        assertEquals(center.x - left.x, right.x - center.x, .0001f)
        assertTrue(left.x < center.x && center.x < right.x)
        assertEquals(left, geometry.eyeAt("L04"))
        assertEquals(left.y, right.y, 0f)
        assertEquals(left.z, right.z, 0f)
        assertEquals(geometry.eyeAt("F08"), geometry.eyeAt("F08", SeatEye.RIGHT))
        for (side in SeatEye.entries) {
            val eye = geometry.eyeAt("L04", side)
            val direction = geometry.direction(SeatCameraState("L04", side = side))
            val distance = (geometry.screenCenter.z - eye.z) / direction.z
            assertEquals(geometry.screenCenter.x, eye.x + direction.x * distance, .0001f)
            assertEquals(geometry.screenCenter.y, eye.y + direction.y * distance, .0001f)
        }
    }

    @Test
    fun oldMiddleViewAndMissingSavedSideRestoreToARealLeftPlace() {
        assertEquals(SeatEye.LEFT, SeatEye.fromSavedValue("CENTER"))
        assertEquals(SeatEye.LEFT, SeatEye.fromSavedValue(null))
        assertEquals(SeatEye.LEFT, SeatEye.fromSavedValue("LEFT"))
        assertEquals(SeatEye.RIGHT, SeatEye.fromSavedValue("RIGHT"))
    }

    @Test
    fun editableDimensionsChangePerspectiveWithoutChangingSeatsOrMovieAspect() {
        val spec = CinemaRoomSpec(roomWidth = 24f, screenWidth = 14f, firstRowDistance = 7f,
            rowSpacing = 1.5f, rowRise = .24f, eyeHeight = 1.35f)
        val adjusted = CinemaSceneGeometry(MockCinemaData.rows, spec)
        assertEquals(geometry.seats.map { it.seat }, adjusted.seats.map { it.seat })
        assertEquals(12f, adjusted.halfWidth, 0f)
        assertEquals(7f, adjusted.eyeAt("A09").z, 0f)
        assertTrue(adjusted.eyeAt("L04").z > geometry.eyeAt("L04").z)
        assertTrue(adjusted.eyeAt("L04").y > geometry.eyeAt("L04").y)
        assertEquals(7f / 3f, adjusted.screenWidth / adjusted.screenHeight, .0001f)
        assertEquals(geometry.spec.horizontalFovDegrees, adjusted.spec.horizontalFovDegrees, 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidDimensionsCannotEnterTheRenderer() {
        CinemaRoomSpec(rowSpacing = Float.NaN)
    }

    @Test
    fun comparisonPinsSeatAndSideButResetsLookForEqualConditions() {
        val comparison = SeatComparison().pin(0, SeatCameraState("F08", 30f, -10f))
            .pin(1, SeatCameraState("L04", -40f, 20f, SeatEye.RIGHT))
        assertEquals(SeatCameraState("F08"), comparison.target(0))
        assertEquals(SeatCameraState("L04", side = SeatEye.RIGHT), comparison.target(1))
        val replaced = comparison.pin(0, SeatCameraState("A09"))
        assertEquals(comparison.b, replaced.b)
        assertEquals("A09", replaced.a?.seatId)
    }
}
