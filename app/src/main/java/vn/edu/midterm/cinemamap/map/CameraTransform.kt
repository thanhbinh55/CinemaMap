package vn.edu.midterm.cinemamap.map

internal data class MapPoint(val x: Float, val y: Float)

internal data class CameraTransform(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float
) {
    fun worldToView(x: Float, y: Float): MapPoint {
        return MapPoint(x * scale + offsetX, y * scale + offsetY)
    }

    fun viewToWorld(x: Float, y: Float): MapPoint {
        return MapPoint((x - offsetX) / scale, (y - offsetY) / scale)
    }
}