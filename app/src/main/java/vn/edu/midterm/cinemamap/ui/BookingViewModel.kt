package vn.edu.midterm.cinemamap.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.edu.midterm.cinemamap.data.MockCinemaData
import vn.edu.midterm.cinemamap.model.Seat
import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatType

class BookingViewModel(private val savedState: SavedStateHandle) : ViewModel() {

    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private fun restoreState(): BookingUiState {
        val seats = MockCinemaData.rows.flatMap { it.seats }.associateBy { it.id }
        val ids = savedState.get<ArrayList<String>>("booking.selected").orEmpty().distinct()
        val selected = ids.mapNotNull(seats::get).filter { it.availability == SeatAvailability.AVAILABLE }
        val filter = SeatFilter(
            type = SeatType.values().firstOrNull { it.name == savedState.get<String>("booking.type") },
            status = SeatStatusFilter.values().firstOrNull { it.name == savedState.get<String>("booking.status") }
        )
        return calculateNewState(BookingUiState(filter = filter), selected,
            savedState.get<String>("booking.inspected")?.let(seats::get))
    }

    private fun updateState(transform: (BookingUiState) -> BookingUiState) {
        _uiState.update(transform)
        val state = _uiState.value
        // Persist IDs/enum names, then derive totals from the fixture when restored.
        savedState["booking.selected"] = ArrayList(state.selectedSeats.map { it.id })
        savedState["booking.inspected"] = state.inspectedSeat?.id
        savedState["booking.type"] = state.filter.type?.name
        savedState["booking.status"] = state.filter.status?.name
    }

    /** Xem thông tin/3D độc lập với chọn vé; cho phép xem cả ghế ngoài bộ lọc. */
    fun inspectSeat(seat: Seat) {
        updateState { it.copy(inspectedSeat = seat) }
    }

    /** Chỉ ghế trống khớp lọc được bật/tắt lựa chọn. */
    fun onSeatTapped(seat: Seat) {
        updateState { currentState ->
            val isSelected = currentState.selectedSeats.any { it.id == seat.id }
            if (!currentState.filter.matches(seat, isSelected)) return@updateState currentState
            if (seat.availability != SeatAvailability.AVAILABLE) {
                return@updateState currentState.copy(inspectedSeat = seat)
            }
            val newSelectedSeats = if (isSelected) {
                currentState.selectedSeats.filterNot { it.id == seat.id }
            } else {
                currentState.selectedSeats + seat
            }

            calculateNewState(currentState, newSelectedSeats, inspectedSeat = seat)
        }
    }

    fun removeSeat(seatId: String) {
        updateState { currentState ->
            val newSelectedSeats = currentState.selectedSeats.filterNot { it.id == seatId }
            val newInspected = if (currentState.inspectedSeat?.id == seatId) null else currentState.inspectedSeat
            calculateNewState(currentState, newSelectedSeats, inspectedSeat = newInspected)
        }
    }

    fun clearSelection() {
        updateState { currentState ->
            calculateNewState(currentState, emptyList(), inspectedSeat = null)
        }
    }

    /** Đổi loại lọc và giữ nguyên danh sách đang chọn. */
    fun setTypeFilter(type: SeatType?) {
        updateState { it.copy(filter = it.filter.copy(type = type)) }
    }

    fun setStatusFilter(status: SeatStatusFilter?) {
        updateState { it.copy(filter = it.filter.copy(status = status)) }
    }

    fun clearFilters() {
        updateState { it.copy(filter = SeatFilter()) }
    }

    private fun calculateNewState(
        currentState: BookingUiState,
        selectedSeats: List<Seat>,
        inspectedSeat: Seat?
    ): BookingUiState {
        val totalSeats = selectedSeats.size
        val totalSpots = selectedSeats.sumOf { it.type.capacity }
        val totalPrice = selectedSeats.sumOf { it.type.priceVnd }

        return currentState.copy(
            selectedSeats = selectedSeats,
            inspectedSeat = inspectedSeat,
            totalSeatsCount = totalSeats,
            totalSpotsCount = totalSpots,
            totalPriceVnd = totalPrice
        )
    }
}
