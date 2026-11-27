package vn.edu.midterm.cinemamap.preview

/** Editable dimensions for a hypothetical room, not measurements of a real cinema. */
internal data class CinemaRoomSpec(
    val roomWidth: Float = 19.12f,
    val screenWidth: Float = 12.6f,
    val firstRowDistance: Float = 4f,
    val rowSpacing: Float = 1.12f,
    val rowRise: Float = 0.18f,
    val eyeHeight: Float = 1.15f,
    val enhancedLighting: Boolean = true
) {
    init {
        require(roomWidth in 16f..24f && screenWidth in 8f..14f)
        require(firstRowDistance in 3f..7f && rowSpacing in 0.9f..1.5f)
        require(rowRise in 0.12f..0.24f && eyeHeight in 0.95f..1.35f)
    }
    val screenHeight: Float get() = screenWidth * 3f / 7f
    val screenCenterY: Float get() = 1.3f + screenHeight / 2f
    val roomHeight: Float get() = maxOf(7.6f, 1.3f + screenHeight + 0.8f)
    val horizontalFovDegrees = 75f
}

internal enum class SeatEye {
    LEFT, RIGHT;

    companion object {
        // The former CENTER camera was between the two real places: migrate to left.
        fun fromSavedValue(value: String?): SeatEye = if (value == RIGHT.name) RIGHT else LEFT
    }
}
