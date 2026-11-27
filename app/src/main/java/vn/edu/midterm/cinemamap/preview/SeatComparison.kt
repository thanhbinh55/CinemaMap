package vn.edu.midterm.cinemamap.preview

internal data class SeatComparison(val a: SeatCameraState? = null, val b: SeatCameraState? = null) {
    fun pin(slot: Int, camera: SeatCameraState): SeatComparison {
        require(slot == 0 || slot == 1)
        val target = camera.copy(yaw = 0f, pitch = 0f)
        return if (slot == 0) copy(a = target) else copy(b = target)
    }

    fun target(slot: Int): SeatCameraState? = if (slot == 0) a else b
}
