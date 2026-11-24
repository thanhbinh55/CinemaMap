package vn.edu.midterm.cinemamap.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatAvailability

class BookingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    /**
     * Xử lý sự kiện khi người dùng chạm vào một ghế trên sơ đồ.
     */
    fun onSeatTapped(seat: Seat) {
        _uiState.update { currentState ->
            if (seat.availability != SeatAvailability.AVAILABLE) {
                return@update currentState.copy(inspectedSeat = seat)
            }
            val isAlreadySelected = currentState.selectedSeats.any { it.id == seat.id }
            val newSelectedSeats = if (isAlreadySelected) {
                currentState.selectedSeats.filterNot { it.id == seat.id }
            } else {
                // Chưa chọn -> Thêm vào cuối danh sách
                currentState.selectedSeats + seat
            }

            // 3. Tính toán lại các thông số dẫn xuất
            calculateNewState(newSelectedSeats, inspectedSeat = seat)
        }
    }

    /**
     * Xử lý sự kiện khi người dùng bấm nút "Xóa" một ghế từ Bottom Sheet.
     */
    fun removeSeat(seatId: String) {
        _uiState.update { currentState ->
            val newSelectedSeats = currentState.selectedSeats.filterNot { it.id == seatId }
            // Nếu ghế vừa bị xóa trùng với inspectedSeat thì có thể xóa hoặc giữ nguyên
            val newInspected = if (currentState.inspectedSeat?.id == seatId) null else currentState.inspectedSeat
            calculateNewState(newSelectedSeats, inspectedSeat = newInspected)
        }
    }

    /**
     * Xóa toàn bộ lựa chọn.
     */
    fun clearSelection() {
        _uiState.update { currentState ->
            calculateNewState(emptyList(), inspectedSeat = null)
        }
    }

    /**
     * Hàm phụ trợ tính toán số lượng chỗ và tổng tiền từ danh sách ghế đã chọn.
     */
    private fun calculateNewState(selectedSeats: List<Seat>, inspectedSeat: Seat?): BookingUiState {
        val totalSeats = selectedSeats.size
        // Mỗi ghế có it.type.capacity (Standard/VIP = 1, Couple = 2)
        val totalSpots = selectedSeats.sumOf { it.type.capacity }
        // Mỗi ghế có it.type.priceVnd (85.000, 110.000, 190.000)
        val totalPrice = selectedSeats.sumOf { it.type.priceVnd }

        return BookingUiState(
            selectedSeats = selectedSeats,
            inspectedSeat = inspectedSeat,
            totalSeatsCount = totalSeats,
            totalSpotsCount = totalSpots,
            totalPriceVnd = totalPrice
        )
    }
}