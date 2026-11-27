package vn.edu.midterm.cinemamap

import android.content.pm.ActivityInfo
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.preview.CinemaPreviewActivity
import vn.edu.midterm.cinemamap.preview.CinemaSceneView
import vn.edu.midterm.cinemamap.preview.SeatCameraState
import vn.edu.midterm.cinemamap.ui.BookingViewModel
import vn.edu.midterm.cinemamap.model.SeatType

@RunWith(AndroidJUnit4::class)
class CinemaPreviewTest {
    @Test
    fun rendersAcrossRotationAndReturnsWithBookingState() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[BookingViewModel::class.java]
                model.onSeatTapped(MockCinemaData.rows.first().seats.first())
                model.setTypeFilter(SeatType.VIP)
                activity.findViewById<android.view.View>(R.id.toggle_sheet).performClick()
            }
            SystemClock.sleep(400)
            onView(withId(R.id.open_3d_preview)).perform(click())
            waitForScene()
            onView(withId(R.id.preview_title)).check(matches(withText("Góc nhìn · A01")))
            assertScenePixels("3d-seat-A01.png")

            onView(withId(R.id.change_preview_seat)).perform(click())
            onData(org.hamcrest.Matchers.equalTo("Hàng L · Ghế đôi")).inRoot(isDialog()).perform(click())
            onData(org.hamcrest.Matchers.equalTo("L04 · Không khả dụng")).inRoot(isDialog()).perform(click())
            onView(withId(R.id.preview_title)).check(matches(withText("Góc nhìn · L04")))
            onView(withId(R.id.preview_scene)).perform(swipeLeft())
            lateinit var beforeRotation: SeatCameraState
            instrumentation.runOnMainSync {
                beforeRotation = currentPreview().findViewById<CinemaSceneView>(R.id.preview_scene).cameraState
                assertNotEquals(0f, beforeRotation.yaw)
            }

            instrumentation.runOnMainSync {
                currentPreview().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            SystemClock.sleep(500)
            waitForScene()
            SystemClock.sleep(700)
            instrumentation.runOnMainSync {
                assertEquals(beforeRotation, currentPreview().findViewById<CinemaSceneView>(R.id.preview_scene).cameraState)
                val activity = currentPreview()
                val scene = activity.findViewById<android.view.View>(R.id.preview_scene_container)
                val panel = activity.findViewById<android.view.View>(R.id.preview_controls_scroll)
                assertTrue("Landscape controls must not cover the 3D viewport", scene.right <= panel.left + 2)
                assertEquals(panel.height, scene.height)
            }
            onView(withId(R.id.reset_3d_look)).perform(click())
            instrumentation.runOnMainSync {
                assertEquals(SeatCameraState("L04"), currentPreview().findViewById<CinemaSceneView>(R.id.preview_scene).cameraState)
            }
            assertScenePixels("3d-seat-L04-landscape.png")

            // Exercise pause/resume, including a surface that may need recreation.
            instrumentation.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
            SystemClock.sleep(200)
            instrumentation.targetContext.startActivity(instrumentation.targetContext.packageManager
                .getLaunchIntentForPackage(instrumentation.targetContext.packageName)!!)
            waitForScene()
            assertScenePixels("3d-seat-L04-resumed.png")
            onView(withId(R.id.close_3d_preview)).perform(click())
            SystemClock.sleep(400)
            scenario.onActivity { activity ->
                val state = ViewModelProvider(activity)[BookingViewModel::class.java].uiState.value
                assertEquals(listOf("A01"), state.selectedSeats.map { it.id })
                assertEquals(SeatType.VIP, state.filter.type)
                assertEquals(85_000, state.totalPriceVnd)
                assertEquals("L04", state.inspectedSeat?.id)
                val map = activity.findViewById<vn.edu.midterm.cinemamap.map.CinemaMapView>(R.id.cinema_map)
                assertEquals("L04", map.javaClass.getDeclaredField("locatedSeatId").apply { isAccessible = true }.get(map))
            }
            onView(withId(R.id.open_3d_preview)).check(matches(withText("Xem góc nhìn 3D · L04")))
        }
    }

    @Test
    fun frontAndBackSeatsHaveDifferentScreenSizeAndCameraSurvivesRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<CinemaPreviewActivity>(Intent(context, CinemaPreviewActivity::class.java)
            .putExtra(CinemaPreviewActivity.EXTRA_SEAT_ID, "A09")).use { scenario ->
            waitForScene()
            val nearScreen = assertScenePixels("3d-seat-A09-front.png")
            scenario.onActivity { it.findViewById<CinemaSceneView>(R.id.preview_scene).showSeat("L04") }
            val farScreen = assertScenePixels("3d-seat-L04-back.png")
            assertTrue("Front seat should see a larger screen: near=$nearScreen far=$farScreen", nearScreen > farScreen * 2)
            onView(withId(R.id.preview_scene)).perform(swipeLeft())
            lateinit var before: SeatCameraState
            scenario.onActivity { before = it.findViewById<CinemaSceneView>(R.id.preview_scene).cameraState }
            scenario.recreate()
            waitForScene()
            scenario.onActivity {
                val scene = it.findViewById<CinemaSceneView>(R.id.preview_scene)
                assertEquals(before, scene.cameraState)
                assertMultiTouchDoesNotChangeLook(scene)
            }
            onView(withId(R.id.preview_scene)).perform(swipeLeft())
            scenario.onActivity { assertNotEquals(before, it.findViewById<CinemaSceneView>(R.id.preview_scene).cameraState) }
        }
    }

    private fun assertMultiTouchDoesNotChangeLook(scene: CinemaSceneView) {
        val before = scene.cameraState
        val down = SystemClock.uptimeMillis()
        var time = down
        val properties = Array(2) { id -> android.view.MotionEvent.PointerProperties().apply {
            this.id = id
            toolType = android.view.MotionEvent.TOOL_TYPE_FINGER
        } }
        val coordinates = Array(2) { index -> android.view.MotionEvent.PointerCoords().apply {
            x = scene.width * if (index == 0) 0.4f else 0.6f
            y = scene.height / 2f
            pressure = 1f
            size = 1f
        } }
        fun send(action: Int, count: Int) {
            val event = android.view.MotionEvent.obtain(down, time, action, count, properties, coordinates,
                0, 0, 1f, 1f, 0, 0, android.view.InputDevice.SOURCE_TOUCHSCREEN, 0)
            scene.dispatchTouchEvent(event)
            event.recycle()
            time += 20
        }
        send(android.view.MotionEvent.ACTION_DOWN, 1)
        send(android.view.MotionEvent.ACTION_POINTER_DOWN or (1 shl android.view.MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2)
        coordinates[0].x = scene.width * 0.1f
        coordinates[1].x = scene.width * 0.9f
        send(android.view.MotionEvent.ACTION_MOVE, 2)
        send(android.view.MotionEvent.ACTION_POINTER_UP or (1 shl android.view.MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2)
        coordinates[0].x = scene.width * 0.8f
        send(android.view.MotionEvent.ACTION_MOVE, 1)
        send(android.view.MotionEvent.ACTION_UP, 1)
        assertEquals("Two-finger input and its remaining finger must not jump the 3D camera", before, scene.cameraState)
    }

    @Test
    fun locatingAnEarlierSelectedSeatUpdatesTheSharedPreviewButtonWithoutUnselecting() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[BookingViewModel::class.java]
                model.onSeatTapped(MockCinemaData.rows.first().seats.first())
                model.onSeatTapped(MockCinemaData.rows[5].seats[7])
                model.setTypeFilter(SeatType.VIP)
                activity.findViewById<android.view.View>(R.id.toggle_sheet).performClick()
            }
            SystemClock.sleep(500)
            scenario.onActivity { activity ->
                activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.selected_seats)
                    .findViewHolderForAdapterPosition(0)!!.itemView.findViewById<android.view.View>(R.id.locate_seat).performClick()
            }
            onView(withId(R.id.open_3d_preview)).check(matches(withText("Xem góc nhìn 3D · A01")))
            onView(withId(R.id.inspected_seat)).check(matches(withText("A01 · Thường · Đang chọn\nNgoài bộ lọc")))
            scenario.recreate()
            onView(withId(R.id.open_3d_preview)).check(matches(withText("Xem góc nhìn 3D · A01")))
            onView(withId(R.id.open_3d_preview)).perform(click())
            waitForScene()
            onView(withId(R.id.preview_title)).check(matches(withText("Góc nhìn · A01")))
            onView(withId(R.id.close_3d_preview)).perform(click())
            scenario.onActivity { activity ->
                val state = ViewModelProvider(activity)[BookingViewModel::class.java].uiState.value
                assertEquals(listOf("A01", "F08"), state.selectedSeats.map { it.id })
                assertEquals(195_000, state.totalPriceVnd)
                assertEquals(SeatType.VIP, state.filter.type)
                assertEquals("A01", state.inspectedSeat?.id)
            }
        }
    }

    @Test
    fun selectedListOpensItsOwnSeatInsteadOfTheLastInspectedSeat() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[BookingViewModel::class.java]
                model.onSeatTapped(MockCinemaData.rows.first().seats.first())
                model.onSeatTapped(MockCinemaData.rows[5].seats[7])
                activity.findViewById<android.view.View>(R.id.toggle_sheet).performClick()
            }
            SystemClock.sleep(500)
            scenario.onActivity { activity ->
                activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.selected_seats)
                    .findViewHolderForAdapterPosition(0)!!.itemView.findViewById<android.view.View>(R.id.preview_seat).performClick()
            }
            waitForScene()
            onView(withId(R.id.preview_title)).check(matches(withText("Góc nhìn · A01")))
            onView(withId(R.id.close_3d_preview)).perform(click())
            scenario.onActivity { assertEquals(listOf("A01", "F08"),
                ViewModelProvider(it)[BookingViewModel::class.java].uiState.value.selectedSeats.map { seat -> seat.id }) }
        }
    }

    @Test
    fun invalidSeatShowsAnErrorWithAWayBack() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<CinemaPreviewActivity>(Intent(context, CinemaPreviewActivity::class.java)
            .putExtra(CinemaPreviewActivity.EXTRA_SEAT_ID, "Z99")).use {
            onView(withId(R.id.preview_error)).check(matches(isDisplayed()))
            onView(withId(R.id.preview_error)).check(matches(withText(R.string.preview_3d_invalid_seat)))
            onView(withId(R.id.close_3d_preview)).perform(click())
        }
    }

    private fun currentPreview(): CinemaPreviewActivity =
        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            .filterIsInstance<CinemaPreviewActivity>().single()

    private fun waitForScene() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val deadline = SystemClock.uptimeMillis() + 8_000
        while (SystemClock.uptimeMillis() < deadline) {
            var ready = false
            instrumentation.runOnMainSync {
                val activity = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED).filterIsInstance<CinemaPreviewActivity>().singleOrNull()
                ready = activity?.findViewById<TextView>(R.id.preview_status)?.text ==
                    instrumentation.targetContext.getString(R.string.preview_3d_ready)
            }
            if (ready) return
            SystemClock.sleep(100)
        }
        fail("3D renderer did not report a successful frame within 8 seconds")
    }

    private fun assertScenePixels(filename: String): Int {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // Surface readiness precedes the system's rotation transition ending.
        SystemClock.sleep(1_000)
        instrumentation.waitForIdleSync()
        val bounds = android.graphics.Rect()
        val miniMapBounds = android.graphics.Rect()
        instrumentation.runOnMainSync {
            currentPreview().findViewById<android.view.View>(R.id.preview_scene_container)
                .getGlobalVisibleRect(bounds)
            currentPreview().findViewById<android.view.View>(R.id.preview_minimap)
                ?.getGlobalVisibleRect(miniMapBounds)
        }
        assertTrue("Scene viewport must have usable space", bounds.width() > 100 && bounds.height() > 100)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        var flame = 0
        var dark = 0
        for (y in bounds.top until bounds.bottom step 5) {
            for (x in bounds.left until bounds.right step 5) {
                if (miniMapBounds.contains(x, y)) continue
                val pixel = bitmap.getPixel(x, y)
                // The current film has a dark background; measure its orange flame,
                // excluding the UI overlay, rather than treating white UI as video.
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                if (red > 150 && green > 50 && red > green * 1.15f && green > blue * 1.3f) flame++
                if (Color.red(pixel) < 120 && Color.green(pixel) < 120 && Color.blue(pixel) < 120) dark++
            }
        }
        assertTrue("The actual screen should show the campfire, not an empty or fallback surface", flame > 20)
        assertTrue("Room should render around the screen", dark > 100)
        java.io.File(instrumentation.targetContext.cacheDir, filename).outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
        return flame
    }
}
