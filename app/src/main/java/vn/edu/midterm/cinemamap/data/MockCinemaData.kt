package vn.edu.midterm.cinemamap.data

import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatRow
import vn.edu.midterm.cinemamap.model.SeatType

object MockCinemaData {
    private val booked = setOf("A03", "A14", "C09", "E05", "E06", "F12", "G13", "H15", "J02", "K02", "L08")
    private val unavailable = setOf("B01", "D18", "F06", "L04")

    val rows: List<SeatRow> = ('A'..'L').map { row ->
        val type = when (row) {
            in 'A'..'D' -> SeatType.STANDARD
            in 'E'..'J' -> SeatType.VIP
            else -> SeatType.COUPLE
        }
        val count = if (type == SeatType.COUPLE) 8 else 18
        val seats = (1..count).map { number ->
            val id = "$row${number.toString().padStart(2, '0')}"
            val availability = when (id) {
                in booked -> SeatAvailability.BOOKED
                in unavailable -> SeatAvailability.UNAVAILABLE
                else -> SeatAvailability.AVAILABLE
            }
            Seat(id, type, availability)
        }
        SeatRow(row, type, seats)
    }
}
