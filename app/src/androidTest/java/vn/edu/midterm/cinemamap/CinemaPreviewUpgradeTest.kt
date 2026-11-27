package vn.edu.midterm.cinemamap

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.button.MaterialButton
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.map.CameraTransform
import vn.edu.midterm.cinemamap.map.CinemaMapView
import vn.edu.midterm.cinemamap.map.MapGeometry
import vn.edu.midterm.cinemamap.preview.*

@RunWith(AndroidJUnit4::class)
class CinemaPreviewUpgradeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val preferences get() = context.getSharedPreferences("cinema_preview_room", Context.MODE_PRIVATE)
    private var savedPreferences: Map<String, *> = emptyMap<String, Any>()

    @Before fun savePreferences() {
        savedPreferences = preferences.all.toMap()
        preferences.edit().clear().commit()
    }

    @After fun restorePreferences() {
        preferences.edit().clear().apply {
            savedPreferences.forEach { (key, value) ->
                when (value) {
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }.commit()
    }

    private fun launch(id: String = "F09") = ActivityScenario.launch<CinemaPreviewActivity>(
        Intent(context, CinemaPreviewActivity::class.java).putExtra(CinemaPreviewActivity.EXTRA_SEAT_ID, id))

    @Test fun comparisonAndCoupleSideSurviveRecreationAndCanBeReplaced() {
        launch().use { scenario ->
            waitForScene(scenario)
            onView(withId(R.id.preview_compare_a)).perform(click())
            scenario.onActivity { it.findViewById<CinemaSceneView>(R.id.preview_scene).showSeat("L04") }
            onView(withId(R.id.preview_side_right)).perform(click())
            onView(withId(R.id.preview_compare_b)).perform(click())
            onView(withId(R.id.toggle_trailer)).check(matches(withText(R.string.trailer_play)))
            onView(withId(R.id.preview_compare_a)).perform(click())
            assertCamera(scenario, SeatCameraState("F09"))
            onView(withId(R.id.preview_compare_b)).perform(click())
            assertCamera(scenario, SeatCameraState("L04", side = SeatEye.RIGHT))
            scenario.recreate()
            waitForScene(scenario)
            assertCamera(scenario, SeatCameraState("L04", side = SeatEye.RIGHT))
            onView(withId(R.id.preview_side_right)).check(matches(isChecked()))
            onView(withId(R.id.preview_compare_b)).check(matches(isChecked()))
            onView(withId(R.id.toggle_trailer)).check(matches(withText(R.string.trailer_play)))
            onView(withId(R.id.preview_compare_a)).perform(click())
            assertCamera(scenario, SeatCameraState("F09"))
            onView(withId(R.id.preview_compare_b)).perform(longClick())
            onView(withId(R.id.preview_compare_b)).check(matches(withText("B · F09")))
            screenshot("3d-upgrade-comparison.png")
        }
    }

    @Test fun miniMapOpensLargeMapAndATapChangesViewpointEvenForUnavailableSeat() {
        launch().use { scenario ->
            waitForScene(scenario)
            onView(withId(R.id.preview_minimap)).perform(click())
            screenshot("3d-upgrade-picker.png")
            onView(withId(R.id.preview_map_picker)).perform(object : ViewAction {
                override fun getConstraints(): Matcher<View> = isAssignableFrom(CinemaMapView::class.java)
                override fun getDescription() = "Tap the real map bounds of unavailable couple seat L04"
                override fun perform(uiController: UiController, view: View) {
                    val map = view as CinemaMapView
                    val camera = map.javaClass.getDeclaredField("camera").apply { isAccessible = true }.get(map) as CameraTransform
                    val geometry = MapGeometry(MockCinemaData.rows)
                    val seat = geometry.rows.flatMap { it.seats }.first { it.seat.id == "L04" }
                    val point = camera.worldToView(seat.left + seat.width / 2f, seat.top + geometry.seatHeight / 2f)
                    val time = SystemClock.uptimeMillis()
                    for ((index, action) in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).withIndex()) {
                        MotionEvent.obtain(time, time + index * 40, action, point.x, point.y, 0).also {
                            map.dispatchTouchEvent(it)
                            it.recycle()
                        }
                    }
                    uiController.loopMainThreadUntilIdle()
                }
            })
            assertCamera(scenario, SeatCameraState("L04"))
            scenario.onActivity {
                assertEquals(2, it.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.preview_seat_side).childCount)
            }
            onView(withId(R.id.preview_minimap)).check(matches(withContentDescription(
                context.getString(R.string.preview_minimap_description, "L04"))))
            onView(withId(R.id.preview_side_right)).perform(click())
            assertCamera(scenario, SeatCameraState("L04", side = SeatEye.RIGHT))
            onView(withId(R.id.preview_side_left)).perform(click())
            assertCamera(scenario, SeatCameraState("L04", side = SeatEye.LEFT))
            screenshot("3d-upgrade-couple.png")
        }
    }

    @Test fun fullscreenSurvivesRotationAndBackRestoresControls() {
        launch("L04").use { scenario ->
            waitForScene(scenario)
            onView(withId(R.id.preview_side_left)).perform(click())
            onView(withId(R.id.preview_options)).perform(click())
            onView(withText(R.string.preview_fullscreen)).perform(click())
            SystemClock.sleep(400)
            scenario.onActivity { activity ->
                assertEquals(View.GONE, activity.findViewById<View>(R.id.preview_toolbar).visibility)
                assertTrue(activity.findViewById<View>(R.id.preview_scene_container).height > activity.resources.displayMetrics.heightPixels * .8)
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            SystemClock.sleep(800)
            waitForScene(scenario)
            // Android can show its own first-use immersive help above the app.
            instrumentation.uiAutomation.rootInActiveWindow?.findAccessibilityNodeInfosByText("Got it")
                ?.firstOrNull()?.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
            SystemClock.sleep(300)
            scenario.onActivity { activity ->
                assertEquals(View.GONE, activity.findViewById<View>(R.id.preview_controls_scroll).visibility)
                val scene = activity.findViewById<View>(R.id.preview_scene_container)
                val root = activity.findViewById<View>(R.id.preview_root)
                assertTrue(scene.width > root.width * .9)
            }
            assertCamera(scenario, SeatCameraState("L04", side = SeatEye.LEFT))
            screenshot("3d-upgrade-fullscreen.png")
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            onView(withId(R.id.preview_toolbar)).check(matches(isDisplayed()))
            onView(withId(R.id.preview_controls_scroll)).check(matches(isDisplayed()))
            scenario.onActivity { activity ->
                val scene = activity.findViewById<View>(R.id.preview_scene_container)
                val panel = activity.findViewById<View>(R.id.preview_controls_scroll)
                assertTrue(scene.right <= panel.left + 2)
            }
            screenshot("3d-upgrade-landscape.png")
        }
    }

    @Test fun applyingRoomDimensionsRecreatesSceneAndKeepsCameraAndPins() {
        launch().use { scenario ->
            waitForScene(scenario)
            onView(withId(R.id.preview_compare_a)).perform(click())
            onView(withId(R.id.preview_options)).perform(click())
            onView(withText(R.string.room_settings_title)).perform(click())
            onView(withContentDescription(R.string.room_width)).perform(object : ViewAction {
                override fun getConstraints(): Matcher<View> = isAssignableFrom(SeekBar::class.java)
                override fun getDescription() = "Set room width to maximum"
                override fun perform(uiController: UiController, view: View) { (view as SeekBar).progress = view.max }
            })
            screenshot("3d-upgrade-room-settings.png")
            onView(withText(R.string.action_apply)).perform(click())
            SystemClock.sleep(500)
            waitForScene(scenario)
            assertCamera(scenario, SeatCameraState("F09"))
            scenario.onActivity { activity ->
                assertEquals(24f, CinemaRoomSettings.load(activity, null).roomWidth, .001f)
                val geometry = activity.javaClass.getDeclaredField("geometry").apply { isAccessible = true }.get(activity) as CinemaSceneGeometry
                assertEquals(12f, geometry.halfWidth, .001f)
            }
            onView(withId(R.id.preview_compare_a)).check(matches(withText("A · F09")))
            onView(withId(R.id.toggle_trailer)).check(matches(withText(R.string.trailer_play)))
        }
    }

    private fun assertCamera(scenario: ActivityScenario<CinemaPreviewActivity>, expected: SeatCameraState) {
        scenario.onActivity { assertEquals(expected, it.findViewById<CinemaSceneView>(R.id.preview_scene).cameraState) }
    }

    private fun waitForScene(scenario: ActivityScenario<CinemaPreviewActivity>) {
        val deadline = SystemClock.uptimeMillis() + 8_000
        while (SystemClock.uptimeMillis() < deadline) {
            var ready = false
            scenario.onActivity { ready = it.findViewById<TextView>(R.id.preview_status).text == context.getString(R.string.preview_3d_ready) &&
                it.findViewById<MaterialButton>(R.id.toggle_trailer).isEnabled }
            if (ready) return
            SystemClock.sleep(100)
        }
        fail("3D and trailer did not become ready")
    }

    private fun screenshot(name: String) {
        SystemClock.sleep(500)
        instrumentation.waitForIdleSync()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        java.io.File(context.cacheDir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
