package vn.edu.midterm.cinemamap

import android.os.SystemClock
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.map.CinemaMapView
import vn.edu.midterm.cinemamap.ui.BookingViewModel

@RunWith(AndroidJUnit4::class)
class BookingSheetTest {
    private fun waitForExpandedSheet(scenario: ActivityScenario<MainActivity>) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        var state = -1
        while (SystemClock.uptimeMillis() < deadline) {
            scenario.onActivity {
                state = BottomSheetBehavior.from(it.findViewById<LinearLayout>(R.id.booking_sheet)).state
            }
            if (state == BottomSheetBehavior.STATE_EXPANDED) return
            SystemClock.sleep(50)
        }
        fail("Sheet did not finish expanding; state=$state")
    }
    @Test
    fun filtersKeepSelectedSeatsInSheetAndSurviveResetAndRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                ViewModelProvider(activity)[BookingViewModel::class.java]
                    .onSeatTapped(MockCinemaData.rows.first().seats.first())
            }
            androidx.test.espresso.Espresso.onView(
                androidx.test.espresso.matcher.ViewMatchers.withId(R.id.filter_type)
            ).perform(androidx.test.espresso.action.ViewActions.click())
            androidx.test.espresso.Espresso.onView(
                androidx.test.espresso.matcher.ViewMatchers.withText(R.string.seat_vip)
            ).inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                .perform(androidx.test.espresso.action.ViewActions.click())
            scenario.onActivity { activity ->
                assertEquals(View.GONE, activity.findViewById<View>(R.id.filter_empty_message).visibility)
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                assertEquals(108, map.matchingSeatIds!!.size)
                assertFalse(map.matchingSeatIds!!.contains("A01"))
                assertEquals(setOf("A01"), map.selectedSeatIds)
                activity.findViewById<View>(R.id.reset_view).performClick()
                activity.findViewById<View>(R.id.toggle_sheet).performClick()
            }
            waitForExpandedSheet(scenario)
            scenario.onActivity { activity ->
                val list = activity.findViewById<RecyclerView>(R.id.selected_seats)
                val row = list.findViewHolderForAdapterPosition(0)!!.itemView
                assertTrue(row.findViewById<TextView>(R.id.locate_seat).text.contains("Ngoài bộ lọc"))
                row.findViewById<View>(R.id.locate_seat).performClick()
                assertEquals(vn.edu.midterm.cinemamap.model.SeatType.VIP,
                    ViewModelProvider(activity)[BookingViewModel::class.java].uiState.value.filter.type)
            }
            scenario.recreate()
            SystemClock.sleep(400)
            scenario.onActivity { activity ->
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                assertEquals(setOf("A01"), map.selectedSeatIds)
                assertEquals(108, map.matchingSeatIds!!.size)
                val model = ViewModelProvider(activity)[BookingViewModel::class.java]
                model.setTypeFilter(vn.edu.midterm.cinemamap.model.SeatType.COUPLE)
                model.setStatusFilter(vn.edu.midterm.cinemamap.ui.SeatStatusFilter.SELECTED)
            }
            SystemClock.sleep(150)
            scenario.onActivity { activity ->
                assertEquals(activity.getString(R.string.filter_empty),
                    activity.findViewById<TextView>(R.id.filter_empty_message).text.toString())
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.filter_empty_message).visibility)
                activity.findViewById<View>(R.id.clear_filters).performClick()
            }
            SystemClock.sleep(150)
            scenario.onActivity { activity ->
                assertEquals(196, activity.findViewById<CinemaMapView>(R.id.cinema_map).matchingSeatIds!!.size)
                assertEquals(View.GONE, activity.findViewById<View>(R.id.filter_empty_message).visibility)
                assertEquals(setOf("A01"), activity.findViewById<CinemaMapView>(R.id.cinema_map).selectedSeatIds)
            }
        }
    }

    @Test
    fun expandedSheetLeavesMapUsableInLandscape() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            SystemClock.sleep(900)
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[BookingViewModel::class.java]
                MockCinemaData.rows.first().seats
                    .filter { it.availability == vn.edu.midterm.cinemamap.model.SeatAvailability.AVAILABLE }
                    .forEach { model.onSeatTapped(it) }
                activity.findViewById<View>(R.id.toggle_sheet).performClick()
            }
            waitForExpandedSheet(scenario)
            scenario.onActivity { activity ->
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                val sheet = activity.findViewById<LinearLayout>(R.id.booking_sheet)
                val list = activity.findViewById<RecyclerView>(R.id.selected_seats)
                assertTrue(map.width > map.height)
                assertTrue("map=${map.width}x${map.height}, sheetTop=${sheet.top}, sheetHeight=${sheet.height}, parentHeight=${(sheet.parent as View).height}, header=${activity.findViewById<View>(R.id.sheet_header).height}, state=${BottomSheetBehavior.from(sheet).state}", map.height > 80)
                val legend = activity.findViewById<View>(R.id.seat_legend)
                assertTrue(map.bottom <= legend.y + 2)
                if (activity.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                    assertTrue(map.right <= activity.findViewById<View>(R.id.booking_content).left + 2)
                    assertTrue(map.height >= activity.findViewById<View>(R.id.booking_content).height * 0.4f)
                } else {
                    assertTrue(legend.y + legend.height <= sheet.top + 2)
                }
                assertTrue(list.height > 0)
                assertTrue(list.canScrollVertically(1))
                assertEquals(BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.from(sheet).state)
                activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    @Test
    fun legendDialogKeepsSelectionAndCanBeClosed() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[BookingViewModel::class.java]
                model.onSeatTapped(MockCinemaData.rows.first().seats.first())
            }
            androidx.test.espresso.Espresso.onView(
                androidx.test.espresso.matcher.ViewMatchers.withId(R.id.legend_details)
            ).perform(androidx.test.espresso.action.ViewActions.click())
            androidx.test.espresso.Espresso.onView(
                androidx.test.espresso.matcher.ViewMatchers.withText(R.string.legend_title)
            ).check(androidx.test.espresso.assertion.ViewAssertions.matches(
                androidx.test.espresso.matcher.ViewMatchers.isDisplayed()
            ))
            androidx.test.espresso.Espresso.onView(
                androidx.test.espresso.matcher.ViewMatchers.withText(R.string.legend_close)
            ).check(androidx.test.espresso.assertion.ViewAssertions.matches(
                androidx.test.espresso.matcher.ViewMatchers.isDisplayed()
            ))
            val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
            val screenshot = instrumentation.uiAutomation.takeScreenshot()
            java.io.File(instrumentation.targetContext.cacheDir, "legend-bold.png").outputStream().use {
                screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            screenshot.recycle()
            androidx.test.espresso.Espresso.onView(
                androidx.test.espresso.matcher.ViewMatchers.withText(R.string.legend_close)
            ).perform(androidx.test.espresso.action.ViewActions.click())
            scenario.onActivity { activity ->
                assertEquals(setOf("A01"),
                    activity.findViewById<CinemaMapView>(R.id.cinema_map).selectedSeatIds)
                val items = activity.findViewById<LinearLayout>(R.id.legend_items)
                assertEquals(6, items.childCount)
                val sample = items.getChildAt(0).findViewById<View>(R.id.legend_sample)
                val visible = android.graphics.Rect()
                assertTrue(sample.getGlobalVisibleRect(visible))
                assertEquals("First legend sample must be fully visible", sample.width, visible.width())
                assertEquals(sample.height, visible.height())
            }
        }
    }

    @Test
    fun summaryListRemovalAndLocationShareState() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            SystemClock.sleep(500)
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[BookingViewModel::class.java]
                val seats = MockCinemaData.rows.flatMap { it.seats }.associateBy { it.id }
                listOf("A05", "E08", "K03").forEach { model.onSeatTapped(seats.getValue(it)) }
                activity.findViewById<View>(R.id.toggle_sheet).performClick()
            }
            waitForExpandedSheet(scenario)
            scenario.onActivity { activity ->
                val sheet = activity.findViewById<LinearLayout>(R.id.booking_sheet)
                assertEquals(BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.from(sheet).state)
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                assertTrue(map.height > 0)
                val legend = activity.findViewById<View>(R.id.seat_legend)
                assertTrue(map.bottom <= legend.y + 2)
                if (activity.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                    assertTrue(map.right <= activity.findViewById<View>(R.id.booking_content).left + 2)
                    assertTrue(map.height >= activity.findViewById<View>(R.id.booking_content).height * 0.4f)
                } else {
                    assertTrue(legend.y + legend.height <= sheet.top + 2)
                }
                val summary = activity.findViewById<TextView>(R.id.booking_summary).text.toString()
                assertTrue(summary.contains("3 ghế"))
                assertTrue(summary.contains("4 chỗ"))
                assertTrue(activity.findViewById<TextView>(R.id.booking_total_price).text.contains("385"))
                val list = activity.findViewById<RecyclerView>(R.id.selected_seats)
                assertEquals(3, list.adapter!!.itemCount)
                list.findViewHolderForAdapterPosition(0)!!.itemView
                    .findViewById<View>(R.id.locate_seat).performClick()
                val located = map.javaClass.getDeclaredField("locatedSeatId").apply { isAccessible = true }
                assertEquals("A05", located.get(map))
                list.findViewHolderForAdapterPosition(0)!!.itemView
                    .findViewById<View>(R.id.remove_seat).performClick()
            }
            SystemClock.sleep(200)
            scenario.onActivity { activity ->
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                assertEquals(setOf("E08", "K03"), map.selectedSeatIds)
                val summary = activity.findViewById<TextView>(R.id.booking_summary).text.toString()
                assertTrue(summary.contains("2 ghế"))
                assertTrue(activity.findViewById<TextView>(R.id.booking_total_price).text.contains("300"))
                activity.findViewById<View>(R.id.reset_view).performClick()
            }
            SystemClock.sleep(350)
            scenario.recreate()
            SystemClock.sleep(500)
            waitForExpandedSheet(scenario)
            scenario.onActivity { activity ->
                assertEquals(setOf("E08", "K03"),
                    activity.findViewById<CinemaMapView>(R.id.cinema_map).selectedSeatIds)
                assertEquals(BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.from(activity.findViewById<LinearLayout>(R.id.booking_sheet)).state)
            }
            val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
            val screenshot = instrumentation.uiAutomation.takeScreenshot()
            java.io.File(instrumentation.targetContext.cacheDir, "navy-selection.png").outputStream().use {
                screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            screenshot.recycle()
        }
    }
}
