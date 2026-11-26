package vn.edu.midterm.cinemamap.ui

import vn.edu.midterm.cinemamap.model.Seat

data class BookingUiState(
    val filter: SeatFilter = SeatFilter(),
    val selectedSeats: List<Seat> = emptyList(),
    val inspectedSeat: Seat? = null,
    val totalSeatsCount: Int = 0,
    val totalSpotsCount: Int = 0,
    val totalPriceVnd: Int = 0
)
