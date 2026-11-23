package vn.edu.midterm.cinemamap.map

import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatRow
import vn.edu.midterm.cinemamap.model.SeatType

internal data class SeatPosition(val seat: Seat, val left: Float, val top: Float, val width: Float)

internal data class RowPosition(
    val label: String,
    val type: SeatType,
    val top: Float,
    val seats: List<SeatPosition>
)

internal class MapGeometry(seatRows: List<SeatRow>) {
    val width = 856f
    val seatHeight = 32f
    val centerX = width / 2f
    val rows: List<RowPosition>
    val height: Float

    init {
        val aisleWidth = 44f
        val seatGap = 6f
        var top = 104f
        var previousType: SeatType? = null

        rows = seatRows.map { row ->
            if (previousType != null && row.type != previousType) {
                top += 18f
            }

            val seatWidth = if (row.type == SeatType.COUPLE) 78f else 36f
            val seatsPerSide = row.seats.size / 2
            val blockWidth = seatsPerSide * seatWidth + (seatsPerSide - 1) * seatGap
            val seats = row.seats.mapIndexed { index, seat ->
                val blockLeft = if (index < seatsPerSide) {
                    centerX - aisleWidth / 2f - blockWidth
                } else {
                    centerX + aisleWidth / 2f
                }
                val column = index % seatsPerSide
                SeatPosition(seat, blockLeft + column * (seatWidth + seatGap), top, seatWidth)
            }

            val position = RowPosition(row.label.toString(), row.type, top, seats)
            top += seatHeight + 8f
            previousType = row.type
            position
        }
        height = top + 16f
    }
}
