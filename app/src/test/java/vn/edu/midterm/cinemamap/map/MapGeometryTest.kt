package vn.edu.midterm.cinemamap.map

import org.junit.Assert.*
import org.junit.Test
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.model.SeatType

class MapGeometryTest {
    private val geometry = MapGeometry(MockCinemaData.rows)
    private val seats = geometry.rows.flatMap { it.seats }

    @Test fun fixtureHas196UniqueSeatsAnd212Spots() {
        assertEquals(196, seats.size)
        assertEquals(196, seats.map { it.seat.id }.toSet().size)
        assertEquals(212, seats.sumOf { it.seat.type.capacity })
        assertEquals(mapOf(SeatType.STANDARD to 72, SeatType.VIP to 108, SeatType.COUPLE to 16),
            seats.groupingBy { it.seat.type }.eachCount())
    }

    @Test fun everySeatUsesInclusiveLeftTopAndExclusiveRightBottom() {
        for (p in seats) {
            assertEquals(p, geometry.findSeatAt(MapPoint(p.left, p.top)))
            assertNull(geometry.findSeatAt(MapPoint(p.left + p.width, p.top + 16f)))
            assertNull(geometry.findSeatAt(MapPoint(p.left + p.width / 2f, p.top + 32f)))
        }
    }

    @Test fun transformedCentersAndBothSidesOfEveryEdgeHitCorrectly() {
        val cameras = listOf(CameraTransform(.43f, 0f, 120f), CameraTransform(1.71f, -730f, 85f),
            CameraTransform(3.2f, 175f, -810f))
        for (camera in cameras) {
            fun hit(x: Float, y: Float): SeatPosition? {
                val screen = camera.worldToView(x, y)
                return geometry.findSeatAt(camera.viewToWorld(screen.x, screen.y))
            }
            for (p in seats) {
                val cx = p.left + p.width / 2f
                val cy = p.top + 16f
                for ((x, y) in listOf(cx to cy, p.left + .1f to cy, p.left + p.width - .1f to cy,
                    cx to p.top + .1f, cx to p.top + 31.9f)) {
                    assertEquals(p, hit(x, y))
                }
                for ((x, y) in listOf(p.left - .1f to cy, p.left + p.width + .1f to cy,
                    cx to p.top - .1f, cx to p.top + 32.1f)) {
                    assertNull(hit(x, y))
                }
            }
            assertNull(hit(geometry.centerX, 300f))
            assertNull(hit(geometry.centerX, 30f))
        }
    }
}
