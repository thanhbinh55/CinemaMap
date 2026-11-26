package vn.edu.midterm.cinemamap.ui

import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatType

enum class SeatStatusFilter {
    AVAILABLE, SELECTED, BOOKED, UNAVAILABLE
}

data class SeatFilter(
    val type: SeatType? = null,
    val status: SeatStatusFilter? = null
) {
    val isActive: Boolean get() = type != null || status != null

    fun matches(seat: Seat, isSelected: Boolean): Boolean {
        val displayedStatus = when {
            isSelected -> SeatStatusFilter.SELECTED
            seat.availability == SeatAvailability.BOOKED -> SeatStatusFilter.BOOKED
            seat.availability == SeatAvailability.UNAVAILABLE -> SeatStatusFilter.UNAVAILABLE
            else -> SeatStatusFilter.AVAILABLE
        }
        return (type == null || type == seat.type) && (status == null || status == displayedStatus)
    }
}
