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
            SystemClock.sleep(600)
            scenario.onActivity { activity ->
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                val sheet = activity.findViewById<LinearLayout>(R.id.booking_sheet)
                val list = activity.findViewById<RecyclerView>(R.id.selected_seats)
                assertTrue(map.width > map.height)
                assertTrue("map=${map.width}x${map.height}, sheetTop=${sheet.top}, sheetHeight=${sheet.height}, parentHeight=${(sheet.parent as View).height}, header=${activity.findViewById<View>(R.id.sheet_header).height}, state=${BottomSheetBehavior.from(sheet).state}", map.height > 80)
                assertTrue(map.bottom <= sheet.top + 2)
                assertTrue(list.height > 0)
                assertTrue(list.canScrollVertically(1))
                assertEquals(BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.from(sheet).state)
                activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
            SystemClock.sleep(800)
            scenario.onActivity { activity ->
                val sheet = activity.findViewById<LinearLayout>(R.id.booking_sheet)
                assertEquals(BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.from(sheet).state)
                val map = activity.findViewById<CinemaMapView>(R.id.cinema_map)
                assertTrue(map.height > 0)
                assertTrue(map.bottom <= sheet.top + 2)
                val summary = activity.findViewById<TextView>(R.id.booking_summary).text.toString()
                assertTrue(summary.contains("3 ghế"))
                assertTrue(summary.contains("4 chỗ"))
                assertTrue(summary.contains("385"))
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
                assertTrue(summary.contains("300"))
                activity.findViewById<View>(R.id.reset_view).performClick()
            }
            SystemClock.sleep(350)
            scenario.recreate()
            SystemClock.sleep(500)
            scenario.onActivity { activity ->
                assertEquals(setOf("E08", "K03"),
                    activity.findViewById<CinemaMapView>(R.id.cinema_map).selectedSeatIds)
                assertEquals(BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.from(activity.findViewById<LinearLayout>(R.id.booking_sheet)).state)
            }
        }
    }
}
