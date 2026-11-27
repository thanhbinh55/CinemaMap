package vn.edu.midterm.cinemamap

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.core.graphics.ColorUtils
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.map.CinemaMapView
import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatType
import vn.edu.midterm.cinemamap.ui.BookingViewModel
import vn.edu.midterm.cinemamap.ui.SeatStatusFilter

@RunWith(AndroidJUnit4::class)
class MapInteractionTest {
    // Inspect the actual camera without adding a testing API to the production View.
    private fun field(o: Any, name: String): Any = o.javaClass.getDeclaredField(name)
        .apply { isAccessible = true }.get(o)!!
    private fun number(o: Any, name: String) = o.javaClass.getMethod("get$name").invoke(o) as Float
    private fun ratio(map: CinemaMapView) = number(field(map, "camera"), "Scale") /
        number(field(map, "fitCamera"), "Scale")
    private fun worldAt(map: CinemaMapView, x: Float, y: Float): Pair<Float, Float> {
        val camera = field(map, "camera")
        return (x - number(camera, "OffsetX")) / number(camera, "Scale") to
            (y - number(camera, "OffsetY")) / number(camera, "Scale")
    }

    @Test fun seatPixelsFollowSelectionRemovalAndFilterAfterZoom() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            SystemClock.sleep(600)
            val seat = MockCinemaData.rows.flatMap { it.seats }.first { it.id == "F08" }
            fun assertSeatColor(expected: (MainActivity) -> Int) {
                val instrumentation = InstrumentationRegistry.getInstrumentation()
                val deadline = SystemClock.uptimeMillis() + 3_000
                var actual = 0
                var wanted = 0
                while (SystemClock.uptimeMillis() < deadline) {
                    instrumentation.waitForIdleSync()
                    var screenX = 0
                    var screenY = 0
                    scenario.onActivity { activity ->
                        val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                        val geometry = field(map, "geometry")
                        val rows = geometry.javaClass.getMethod("getRows").invoke(geometry) as List<*>
                        val positions = rows.flatMap { it!!.javaClass.getMethod("getSeats").invoke(it) as List<*> }
                        val p = positions.first {
                            (it!!.javaClass.getMethod("getSeat").invoke(it) as Seat).id == seat.id
                        }!!
                        val camera = field(map, "camera")
                        val origin = IntArray(2)
                        map.getLocationOnScreen(origin)
                        screenX = ((number(p, "Left") + 8f) * number(camera, "Scale") + number(camera, "OffsetX")).toInt() + origin[0]
                        screenY = ((number(p, "Top") + 6f) * number(camera, "Scale") + number(camera, "OffsetY")).toInt() + origin[1]
                        wanted = expected(activity)
                    }
                    val bitmap = instrumentation.uiAutomation.takeScreenshot()
                    actual = bitmap.getPixel(screenX, screenY)
                    bitmap.recycle()
                    if (kotlin.math.abs(Color.red(wanted) - Color.red(actual)) <= 1 &&
                        kotlin.math.abs(Color.green(wanted) - Color.green(actual)) <= 1 &&
                        kotlin.math.abs(Color.blue(wanted) - Color.blue(actual)) <= 1) return
                    SystemClock.sleep(50)
                }
                fail("Seat color expected=${wanted.toUInt().toString(16)} actual=${actual.toUInt().toString(16)}")
            }
            scenario.onActivity { activity ->
                activity.findViewById<CinemaMapView>(R.id.cinema_map).locateSeat(seat.id)
                ViewModelProvider(activity)[BookingViewModel::class.java].onSeatTapped(seat)
            }
            assertSeatColor { it.getColor(R.color.seat_accent) }
            scenario.onActivity { ViewModelProvider(it)[BookingViewModel::class.java].removeSeat(seat.id) }
            assertSeatColor { it.getColor(R.color.seat_vip) }
            scenario.onActivity {
                ViewModelProvider(it)[BookingViewModel::class.java].setTypeFilter(SeatType.STANDARD)
            }
            assertSeatColor { ColorUtils.compositeColors(ColorUtils.setAlphaComponent(it.getColor(R.color.seat_vip), 55), Color.WHITE) }
            scenario.onActivity { activity ->
                ViewModelProvider(activity)[BookingViewModel::class.java].clearFilters()
                activity.findViewById<CinemaMapView>(R.id.cinema_map).locateSeat(seat.id)
            }
            assertSeatColor { it.getColor(R.color.seat_vip) }
        }
    }

    private fun pinch(map: CinemaMapView, spans: List<Float>) {
        val cx = map.width * .52f
        val cy = map.height * .47f
        val time = SystemClock.uptimeMillis()
        fun send(action: Int, span: Float, count: Int, step: Int) {
            val properties = Array(count) { MotionEvent.PointerProperties().apply {
                id = it
                toolType = MotionEvent.TOOL_TYPE_FINGER
            } }
            val coordinates = Array(count) { MotionEvent.PointerCoords().apply {
                x = cx + if (it == 0) -span / 2 else span / 2
                y = cy
                pressure = 1f
                size = 1f
            } }
            MotionEvent.obtain(time, time + step * 40, action, count, properties, coordinates,
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0).also {
                map.dispatchTouchEvent(it)
                it.recycle()
            }
        }
        send(MotionEvent.ACTION_DOWN, spans.first(), 1, 0)
        send(MotionEvent.ACTION_POINTER_DOWN or (1 shl 8), spans.first(), 2, 1)
        spans.forEachIndexed { index, span -> send(MotionEvent.ACTION_MOVE, span, 2, index + 2) }
        send(MotionEvent.ACTION_POINTER_UP or (1 shl 8), spans.last(), 2, spans.size + 2)
        send(MotionEvent.ACTION_UP, spans.last(), 1, spans.size + 3)
    }

    @Test fun pinchPreservesAnchorHonorsLimitsAndPanCannotLoseMap() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            SystemClock.sleep(600)
            scenario.onActivity { activity ->
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                map.locateSeat("F08")
                var tapCount = 0
                map.onSeatClickListener = { tapCount++ }
                val before = worldAt(map, map.width * .52f, map.height * .47f)
                pinch(map, listOf(400f, 450f, 500f, 600f, 700f, 800f))
                assertTrue(ratio(map) > 2.5f)
                val after = worldAt(map, map.width * .52f, map.height * .47f)
                assertEquals(before.first, after.first, .01f)
                assertEquals(before.second, after.second, .01f)
                repeat(4) { pinch(map, listOf(400f, 450f, 500f, 600f, 700f, 800f)) }
                assertEquals(5f, ratio(map), .01f)
                repeat(7) { pinch(map, listOf(800f, 700f, 600f, 500f, 400f)) }
                assertEquals(.8f, ratio(map), .01f)
                assertEquals("Pinch must not create a tap", 0, tapCount)
                map.locateSeat("F08")
                val pan = map.javaClass.getDeclaredMethod("panBy", Float::class.javaPrimitiveType,
                    Float::class.javaPrimitiveType).apply { isAccessible = true }
                for (dx in listOf(-100000f, 100000f)) for (dy in listOf(-100000f, 100000f)) {
                    pan.invoke(map, dx, dy)
                    val camera = field(map, "camera")
                    val scale = number(camera, "Scale")
                    val x = number(camera, "OffsetX")
                    val y = number(camera, "OffsetY")
                    assertTrue(x < map.width && y < map.height)
                    assertTrue(x + 856 * scale > 0 && y + 636 * scale > 0)
                }
            }
        }
    }

    @Test fun rotationKeepsCameraSelectionFilterSheetAndCorrectToggleLabel() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            SystemClock.sleep(700)
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[BookingViewModel::class.java]
                val seats = MockCinemaData.rows.flatMap { it.seats }.associateBy { it.id }
                listOf("A05", "E08", "K03").forEach { model.onSeatTapped(seats.getValue(it)) }
                model.setStatusFilter(SeatStatusFilter.SELECTED)
                activity.findViewById<View>(R.id.toggle_sheet).performClick()
            }
            SystemClock.sleep(600)
            scenario.onActivity { activity ->
                activity.findViewById<CinemaMapView>(R.id.cinema_map).locateSeat("F08")
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            SystemClock.sleep(1000)
            repeat(2) {
                scenario.onActivity { activity ->
                    val model = ViewModelProvider(activity)[BookingViewModel::class.java]
                    val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                    assertEquals(setOf("A05", "E08", "K03"), map.selectedSeatIds)
                    assertEquals(map.selectedSeatIds, map.matchingSeatIds)
                    assertEquals(385000, model.uiState.value.totalPriceVnd)
                    assertEquals(SeatStatusFilter.SELECTED, model.uiState.value.filter.status)
                    assertEquals(2.5f, ratio(map), .01f)
                    assertEquals(BottomSheetBehavior.STATE_EXPANDED,
                        BottomSheetBehavior.from(activity.findViewById<LinearLayout>(R.id.booking_sheet)).state)
                    assertEquals(activity.getString(R.string.collapse_sheet),
                        activity.findViewById<TextView>(R.id.toggle_sheet).text.toString())
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                SystemClock.sleep(1000)
            }
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
        }
    }
}
