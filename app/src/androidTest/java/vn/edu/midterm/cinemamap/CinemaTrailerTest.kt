package vn.edu.midterm.cinemamap

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import vn.edu.midterm.cinemamap.preview.CinemaPreviewActivity

@RunWith(AndroidJUnit4::class)
class CinemaTrailerTest {
    @Test
    fun bundledClipIsFiveSecondWidescreenVideoWithoutAudio() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val retriever = MediaMetadataRetriever()
        try {
            context.resources.openRawResourceFd(R.raw.campfire_loop).use {
                retriever.setDataSource(it.fileDescriptor, it.startOffset, it.length)
                assertEquals("5000", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
                assertEquals("1008", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                assertEquals("432", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
                assertNotEquals("yes", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO))
            }
        } finally {
            retriever.release()
        }
    }

    @Test
    fun actualScreenAnimatesPausesRestoresAndLoopsAfterBackgrounding() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        // Isolate this playback lifecycle from preview tasks left by other UI tests.
        ActivityScenario.launch<CinemaPreviewActivity>(Intent(context, CinemaPreviewActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra(CinemaPreviewActivity.EXTRA_SEAT_ID, "F09")).use { scenario ->
            waitForPlayback(scenario, R.string.trailer_pause)
            SystemClock.sleep(500)
            val first = screenPatch(scenario)
            SystemClock.sleep(750)
            val second = screenPatch(scenario)
            SystemClock.sleep(750)
            val third = screenPatch(scenario)
            assertTrue("Movie pixels on the actual 3D screen must change", maxOf(changedFraction(first, second), changedFraction(first, third)) > 0.02)

            onView(withId(R.id.toggle_trailer)).perform(click())
            waitForPlayback(scenario, R.string.trailer_play)
            SystemClock.sleep(400)
            val paused = screenPatch(scenario)
            SystemClock.sleep(600)
            assertTrue("Paused screen must remain still", changedFraction(paused, screenPatch(scenario)) < 0.01)

            scenario.recreate()
            waitForPlayback(scenario, R.string.trailer_play)
            onView(withId(R.id.toggle_trailer)).perform(click())
            waitForPlayback(scenario, R.string.trailer_pause)
            scenario.moveToState(Lifecycle.State.CREATED)
            SystemClock.sleep(300)
            scenario.moveToState(Lifecycle.State.RESUMED)
            waitForPlayback(scenario, R.string.trailer_pause)
            SystemClock.sleep(5_200)
            val loopA = screenPatch(scenario, "campfire-loop-a.png")
            var positionA = -1
            var positionB = -1
            scenario.onActivity {
                val player = it.javaClass.getDeclaredField("trailer").apply { isAccessible = true }.get(it)
                    as vn.edu.midterm.cinemamap.preview.CinemaTrailerPlayer
                positionA = player.positionMs()
            }
            SystemClock.sleep(700)
            val loopB = screenPatch(scenario, "campfire-loop-b.png")
            SystemClock.sleep(700)
            scenario.onActivity {
                val player = it.javaClass.getDeclaredField("trailer").apply { isAccessible = true }.get(it)
                    as vn.edu.midterm.cinemamap.preview.CinemaTrailerPlayer
                positionB = player.positionMs()
            }
            val loopMotion = maxOf(changedFraction(loopA, loopB), changedFraction(loopA, screenPatch(scenario)))
            assertNotEquals("Decoder timestamp must advance after returning to the app", positionA, positionB)
            assertTrue("Video must animate after looping: motion=$loopMotion position=$positionA->$positionB", loopMotion > 0.02)

            // A second background/foreground cycle must create a new decoder,
            // retain playback intent, and deliver fresh frames to the GL screen.
            scenario.moveToState(Lifecycle.State.CREATED)
            SystemClock.sleep(300)
            scenario.moveToState(Lifecycle.State.RESUMED)
            waitForPlayback(scenario, R.string.trailer_pause)
            val resumed = screenPatch(scenario)
            SystemClock.sleep(800)
            assertTrue("Video must animate after a second resume",
                changedFraction(resumed, screenPatch(scenario)) > 0.02)

            val screenshot = instrumentation.uiAutomation.takeScreenshot()
            java.io.File(context.cacheDir, "3d-trailer-playing.png").outputStream().use {
                screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            screenshot.recycle()
        }
    }

    private fun waitForPlayback(scenario: ActivityScenario<CinemaPreviewActivity>, label: Int) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            var ready = false
            scenario.onActivity {
                val button = it.findViewById<TextView>(R.id.toggle_trailer)
                val player = it.javaClass.getDeclaredField("trailer").apply { isAccessible = true }.get(it)
                    as? vn.edu.midterm.cinemamap.preview.CinemaTrailerPlayer
                ready = button.isEnabled && button.text == it.getString(label) &&
                    player?.playRequested == (label == R.string.trailer_pause) &&
                    it.findViewById<View>(R.id.preview_status).visibility == View.GONE
            }
            if (ready) {
                // Surface readiness precedes the window/layout recreation transition.
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                SystemClock.sleep(350)
                return
            }
            SystemClock.sleep(100)
        }
        fail("Trailer did not reach playback state $label")
    }

    private fun screenPatch(scenario: ActivityScenario<CinemaPreviewActivity>, filename: String? = null): IntArray {
        val bounds = Rect()
        scenario.onActivity { it.findViewById<View>(R.id.preview_scene_container).getGlobalVisibleRect(bounds) }
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        if (filename != null) java.io.File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, filename).outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        // Camera initially targets the screen centre, so this patch lies inside the screen plane.
        val values = ArrayList<Int>()
        val halfHeight = minOf(bounds.height() / 16, bounds.width() / 20)
        for (y in bounds.centerY() - halfHeight until bounds.centerY() + halfHeight step 4) {
            for (x in bounds.centerX() - bounds.width() / 10 until bounds.centerX() + bounds.width() / 10 step 4) {
                values.add(bitmap.getPixel(x, y))
            }
        }
        bitmap.recycle()
        return values.toIntArray()
    }

    private fun changedFraction(a: IntArray, b: IntArray): Double {
        assertEquals(a.size, b.size)
        return a.indices.count { i ->
            kotlin.math.abs(Color.red(a[i]) - Color.red(b[i])) +
                kotlin.math.abs(Color.green(a[i]) - Color.green(b[i])) +
                kotlin.math.abs(Color.blue(a[i]) - Color.blue(b[i])) > 3
        }.toDouble() / a.size
    }
}
