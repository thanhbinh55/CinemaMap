package vn.edu.midterm.cinemamap.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatType

class BookingViewModelTest {

    @Test
    fun savedStateRestoresSelectionFilterInspectionAndRecalculatesTotals() {
        val handle = androidx.lifecycle.SavedStateHandle()
        val first = BookingViewModel(handle)
        val seats = vn.edu.midterm.cinemamap.data.MockCinemaData.rows.flatMap { it.seats }.associateBy { it.id }
        listOf("A05", "E08", "K03").forEach { first.onSeatTapped(seats.getValue(it)) }
        first.setTypeFilter(SeatType.VIP)
        first.setStatusFilter(SeatStatusFilter.SELECTED)
        first.inspectSeat(seats.getValue("L04"))
        val snapshot = handle.keys().associateWith { handle.get<Any?>(it) }
        val restored = BookingViewModel(androidx.lifecycle.SavedStateHandle(snapshot))
        assertEquals(first.uiState.value, restored.uiState.value)
        restored.removeSeat("A05")
        assertEquals(300_000, restored.uiState.value.totalPriceVnd)
    }

    @Test
    fun restorationDropsUnknownDuplicateAndUnavailableSelections() {
        val model = BookingViewModel(androidx.lifecycle.SavedStateHandle(mapOf(
            "booking.selected" to arrayListOf("A05", "A05", "Z99", "A03", "B01", "K03"),
            "booking.type" to "OLD_TYPE", "booking.status" to "OLD_STATUS",
            "booking.inspected" to "Z99"
        )))
        assertEquals(listOf("A05", "K03"), model.uiState.value.selectedSeats.map { it.id })
        assertEquals(275_000, model.uiState.value.totalPriceVnd)
        assertEquals(SeatFilter(), model.uiState.value.filter)
        assertEquals(null, model.uiState.value.inspectedSeat)
    }

    @Test
    fun inspectingSeatsDoesNotToggleSelectionOrChangeFilterAndTotals() {
        val model = BookingViewModel(androidx.lifecycle.SavedStateHandle())
        val standard = Seat("A05", SeatType.STANDARD, SeatAvailability.AVAILABLE)
        val vip = Seat("E08", SeatType.VIP, SeatAvailability.AVAILABLE)
        model.onSeatTapped(standard)
        model.onSeatTapped(vip)
        model.setTypeFilter(SeatType.VIP)
        val before = model.uiState.value

        model.inspectSeat(standard)
        assertEquals(before.copy(inspectedSeat = standard), model.uiState.value)
        model.inspectSeat(standard)
        assertEquals(before.copy(inspectedSeat = standard), model.uiState.value)

        val booked = Seat("A03", SeatType.STANDARD, SeatAvailability.BOOKED)
        model.inspectSeat(booked)
        assertEquals(before.copy(inspectedSeat = booked), model.uiState.value)
    }

    @Test
    fun testSelectSeatsAndCalculatePrice_CaseT02() {
        val viewModel = BookingViewModel(androidx.lifecycle.SavedStateHandle())

        val seatStandard = Seat("A05", SeatType.STANDARD, SeatAvailability.AVAILABLE)
        val seatVip = Seat("E08", SeatType.VIP, SeatAvailability.AVAILABLE)
        val seatCouple = Seat("K03", SeatType.COUPLE, SeatAvailability.AVAILABLE)
        val seatBooked = Seat("A03", SeatType.STANDARD, SeatAvailability.BOOKED)

        viewModel.onSeatTapped(seatStandard)
        viewModel.onSeatTapped(seatVip)
        viewModel.onSeatTapped(seatCouple)

        val state1 = viewModel.uiState.value
        assertEquals(3, state1.totalSeatsCount)
        assertEquals(4, state1.totalSpotsCount)
        assertEquals(385_000, state1.totalPriceVnd)

        viewModel.onSeatTapped(seatVip)
        val state2 = viewModel.uiState.value
        assertEquals(2, state2.totalSeatsCount)
        assertEquals(3, state2.totalSpotsCount)
        assertEquals(275_000, state2.totalPriceVnd)

        viewModel.onSeatTapped(seatBooked)
        val state3 = viewModel.uiState.value
        assertEquals(2, state3.totalSeatsCount)
        assertEquals("A03", state3.inspectedSeat?.id)
    }
}
