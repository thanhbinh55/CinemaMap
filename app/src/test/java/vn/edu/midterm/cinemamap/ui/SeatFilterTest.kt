package vn.edu.midterm.cinemamap.ui

import org.junit.Assert.*
import org.junit.Test
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.model.SeatType

class SeatFilterTest {
    private val seats = MockCinemaData.rows.flatMap { it.seats }
    private val standard = seats.first { it.id == "A05" }
    private val vip = seats.first { it.id == "E08" }

    private fun matching(model: BookingViewModel): List<String> {
        val state = model.uiState.value
        val selected = state.selectedSeats.map { it.id }.toSet()
        return seats.filter { state.filter.matches(it, it.id in selected) }.map { it.id }
    }

    @Test
    fun filtersCombineAndKeepSelectionsAndTotals() {
        val model = BookingViewModel()
        model.onSeatTapped(standard)
        model.setTypeFilter(SeatType.VIP)
        assertEquals(108, matching(model).size)
        assertEquals(listOf(standard), model.uiState.value.selectedSeats)
        model.onSeatTapped(standard)
        assertEquals(listOf(standard), model.uiState.value.selectedSeats)
        model.onSeatTapped(vip)
        model.setStatusFilter(SeatStatusFilter.SELECTED)
        assertEquals(listOf("E08"), matching(model))
        model.removeSeat("E08")
        assertTrue(matching(model).isEmpty())
        assertEquals(85_000, model.uiState.value.totalPriceVnd)
        assertEquals(SeatType.VIP, model.uiState.value.filter.type)
        model.clearFilters()
        assertEquals(196, matching(model).size)
        assertEquals(listOf(standard), model.uiState.value.selectedSeats)
    }

    @Test
    fun availableFilterUpdatesImmediatelyAfterSelection() {
        val model = BookingViewModel()
        model.setStatusFilter(SeatStatusFilter.AVAILABLE)
        assertEquals(181, matching(model).size)
        model.onSeatTapped(standard)
        assertFalse("A05" in matching(model))
        assertEquals(180, matching(model).size)
        model.clearSelection()
        assertEquals(181, matching(model).size)
        assertEquals(SeatStatusFilter.AVAILABLE, model.uiState.value.filter.status)
    }

    @Test
    fun emptyCombinationAndBookedSeatsDoNotChangeSelection() {
        val model = BookingViewModel()
        model.setTypeFilter(SeatType.COUPLE)
        model.setStatusFilter(SeatStatusFilter.SELECTED)
        assertTrue(matching(model).isEmpty())
        model.clearFilters()
        model.setStatusFilter(SeatStatusFilter.BOOKED)
        val booked = seats.first { it.id == "A03" }
        model.onSeatTapped(booked)
        assertEquals(booked, model.uiState.value.inspectedSeat)
        assertTrue(model.uiState.value.selectedSeats.isEmpty())
        assertEquals(11, matching(model).size)
    }
}
