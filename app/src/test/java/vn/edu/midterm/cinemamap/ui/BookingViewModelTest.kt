package vn.edu.midterm.cinemamap.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatType

class BookingViewModelTest {

    @Test
    fun testSelectSeatsAndCalculatePrice_CaseT02() {
        val viewModel = BookingViewModel()

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
