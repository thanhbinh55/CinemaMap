package vn.edu.midterm.cinemamap.model

enum class SeatType(val capacity: Int, val priceVnd: Int) {
    STANDARD(1, 85_000),
    VIP(1, 110_000),
    COUPLE(2, 190_000)
}

enum class SeatAvailability {
    AVAILABLE,
    BOOKED,
    UNAVAILABLE
}

data class Seat(
    val id: String,
    val type: SeatType,
    val availability: SeatAvailability
)

data class SeatRow(
    val label: Char,
    val type: SeatType,
    val seats: List<Seat>
)
