package vn.edu.midterm.cinemamap

import android.graphics.RectF
import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import vn.edu.midterm.cinemamap.map.CinemaMapView

@RunWith(AndroidJUnit4::class)
class MapNavigationTest {
    @Test
    fun panAtFitIsBoundedAndMiniMapTracksZoom() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            SystemClock.sleep(500)
            lateinit var map: CinemaMapView
            var originalOffset = 0f
            var expectedScale = 0f
            fun cameraNumber(name: String): Float {
                val camera = map.javaClass.getDeclaredField("camera").apply { isAccessible = true }.get(map)
                return camera.javaClass.getMethod(name).invoke(camera) as Float
            }
            scenario.onActivity { activity ->
                map = activity.findViewById(R.id.cinema_map)
                originalOffset = cameraNumber("getOffsetX")
                expectedScale = cameraNumber("getScale")
                val time = SystemClock.uptimeMillis()
                for ((index, action) in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP).withIndex()) {
                    val x = map.width / 2f + if (index == 0) 0f else 80f
                    val event = MotionEvent.obtain(time, time + index * 40, action, x, map.height / 2f, 0)
                    map.dispatchTouchEvent(event)
                    event.recycle()
                }
                assertTrue(cameraNumber("getOffsetX") > originalOffset)
                assertEquals(expectedScale, cameraNumber("getScale"), 0.001f)
                map.locateSeat("F08")
            }
            SystemClock.sleep(300)
            scenario.onActivity {
                val bounds = map.javaClass.getDeclaredField("miniBounds").apply { isAccessible = true }.get(map) as RectF
                val viewport = map.javaClass.getDeclaredField("miniViewport").apply { isAccessible = true }.get(map) as RectF
                assertFalse(bounds.isEmpty)
                assertFalse(viewport.isEmpty)
                assertTrue(bounds.right <= map.width)
                assertTrue(viewport.left >= 0f && viewport.right <= 856f)
                assertTrue(viewport.top >= 0f && viewport.bottom <= 636f)
            }
        }
    }

    @Test
    fun landscapeMapAndSeatCodesRemainVisible() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            SystemClock.sleep(800)
            scenario.onActivity { activity ->
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                map.locateSeat("F08")
                activity.findViewById<android.view.View>(R.id.toggle_sheet).performClick()
            }
            SystemClock.sleep(600)
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val screenshot = instrumentation.uiAutomation.takeScreenshot()
            java.io.File(instrumentation.targetContext.cacheDir, "landscape-review.png").outputStream().use {
                screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            scenario.onActivity { activity ->
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                val panel = activity.findViewById<android.view.View>(R.id.booking_content)
                assertTrue(map.right <= panel.left + 2)
                assertTrue(map.height >= panel.height * 0.4f)
                activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }
}
