package vn.edu.midterm.cinemamap.preview

import vn.edu.midterm.cinemamap.model.SeatAvailability
import vn.edu.midterm.cinemamap.model.SeatType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

internal data class ScenePalette(
    val standard: Int, val vip: Int, val couple: Int,
    val booked: Int, val unavailable: Int, val selected: Int
)

/** Builds geometry once per seat change/context creation, never during a drag frame. */
internal class CinemaSceneMesh(
    private val geometry: CinemaSceneGeometry,
    private val palette: ScenePalette,
    private val selectedIds: Set<String>
) {
    fun room(): FloatBuffer = MeshBuilder().apply {
        val w = geometry.halfWidth
        val d = geometry.depth
        val h = geometry.spec.roomHeight
        box(-w, -0.1f, -0.1f, w, 0f, d, 0x263443)
        box(-w - 0.15f, 0f, -0.2f, w + 0.15f, h, 0f, 0x182637)
        box(-w - 0.15f, 0f, 0f, -w, h, d, 0x243447)
        box(w, 0f, 0f, w + 0.15f, h, d, 0x243447)
        box(-w, 0f, d, w, h, d + 0.15f, 0x182637)
        box(-w, h, 0f, w, h + .1f, d, 0x101A29)
        for (row in geometry.rows) {
            box(-w, 0f, row.frontZ, w, row.floorY, row.backZ, 0x344356)
            // The shared layout's central aisle stays clear of chairs.
            box(-0.39f, row.floorY, row.frontZ, 0.39f, row.floorY + 0.012f, row.backZ, 0x53647A)
            box(-0.39f, row.floorY + 0.013f, row.frontZ, 0.39f, row.floorY + 0.022f, row.frontZ + 0.025f, 0xD3AE69)
            // Warm, low step lights at the outer aisles.
            box(-w + 0.15f, row.floorY + 0.015f, row.frontZ + 0.03f,
                -w + 0.65f, row.floorY + 0.028f, row.frontZ + 0.065f, 0xD3AE69)
            box(w - 0.65f, row.floorY + 0.015f, row.frontZ + 0.03f,
                w - 0.15f, row.floorY + 0.028f, row.frontZ + 0.065f, 0xD3AE69)
        }
        // Acoustic wall panels create depth cues even when looking sideways.
        for (z in 1 until d.toInt() step 2) {
            box(-w + 0.01f, 2.8f, z.toFloat(), -w + 0.07f, h - .9f, z + 0.7f, 0x304157)
            box(w - 0.07f, 2.8f, z.toFloat(), w - 0.01f, h - .9f, z + 0.7f, 0x304157)
        }
        val sx = geometry.screenWidth / 2f
        val sy = geometry.screenHeight / 2f
        val cy = geometry.screenCenter.y
        box(-sx - 0.18f, cy - sy - 0.18f, 0f, sx + 0.18f, cy + sy + 0.18f, 0.08f, 0x080F1B)
        // Emissive-style gradient is illustrative, not a physically based light simulation.
        quad(
            Point3(-sx, cy - sy, 0.12f), Point3(sx, cy - sy, 0.12f),
            Point3(sx, cy + sy, 0.12f), Point3(-sx, cy + sy, 0.12f),
            listOf(0xB4D2F1, 0xB4D2F1, 0xE6F3FF, 0xE6F3FF)
        )
    }.buffer()

    fun chairs(viewedSeatId: String): FloatBuffer = MeshBuilder().apply {
        for (position in geometry.seats) {
            // Own chair is hidden to prevent looking through its backrest/armrests.
            if (position.seat.id == viewedSeatId) continue
            val seat = position.seat
            val color = when {
                seat.id in selectedIds -> palette.selected
                seat.availability == SeatAvailability.BOOKED -> palette.booked
                seat.availability == SeatAvailability.UNAVAILABLE -> palette.unavailable
                seat.type == SeatType.VIP -> palette.vip
                seat.type == SeatType.COUPLE -> palette.couple
                else -> palette.standard
            }
            val x = position.center.x
            val y = position.center.y
            val z = position.center.z
            val half = position.width / 2f - 0.025f
            box(x - half + 0.1f, y + 0.06f, z - 0.22f, x + half - 0.1f, y + 0.4f, z + 0.22f, 0x172130)
            box(x - half + 0.055f, y + 0.4f, z - 0.34f, x + half - 0.055f, y + 0.54f, z + 0.22f, color)
            roundedBack(x - half + 0.04f, y + 0.44f, z + 0.22f, position.width - 0.13f, 0.67f, 0.14f, color)
            if (geometry.spec.enhancedLighting) {
                box(x - half + .09f, y + .76f, z + .36f, x + half - .09f, y + .99f, z + .375f, shade(color, 1.04f))
            }
            box(x - half, y + 0.44f, z - 0.28f, x - half + 0.07f, y + 0.68f, z + 0.25f, 0x233044)
            box(x + half - 0.07f, y + 0.44f, z - 0.28f, x + half, y + 0.68f, z + 0.25f, 0x233044)
            if (seat.type == SeatType.COUPLE) {
                // A wide two-person cushion, with no central armrest.
                box(x - 0.012f, y + 0.54f, z - 0.3f, x + 0.012f, y + 0.545f, z + 0.18f, shade(color, 0.7f))
            }
        }
    }.buffer()

    fun shadows(viewedSeatId: String): FloatBuffer = MeshBuilder().apply {
        if (geometry.spec.enhancedLighting) {
            geometry.seats.filter { it.seat.id != viewedSeatId }.forEach {
                shadow(it.center.copy(y = it.center.y + .004f), it.width * .48f, .4f)
            }
        }
    }.buffer()
}

private class MeshBuilder {
    private val values = ArrayList<Float>()

    private fun vertex(p: Point3, color: Int) {
        values.add(p.x)
        values.add(p.y)
        values.add(p.z)
        values.add((color shr 16 and 255) / 255f)
        values.add((color shr 8 and 255) / 255f)
        values.add((color and 255) / 255f)
    }

    fun quad(a: Point3, b: Point3, c: Point3, d: Point3, colors: List<Int>) {
        for ((p, color) in listOf(a to colors[0], b to colors[1], c to colors[2], a to colors[0], c to colors[2], d to colors[3])) vertex(p, color)
    }

    private fun face(a: Point3, b: Point3, c: Point3, d: Point3, color: Int) = quad(a, b, c, d, List(4) { color })

    fun shadow(center: Point3, rx: Float, rz: Float) {
        for (i in 0 until 18) {
            val a = i * Math.PI * 2 / 18
            val b = (i + 1) * Math.PI * 2 / 18
            vertex(center, 0x172434)
            vertex(Point3(center.x + cos(a).toFloat() * rx, center.y, center.z + sin(a).toFloat() * rz), 0x38485C)
            vertex(Point3(center.x + cos(b).toFloat() * rx, center.y, center.z + sin(b).toFloat() * rz), 0x38485C)
        }
    }

    fun box(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, color: Int) {
        face(Point3(x0,y0,z0), Point3(x1,y0,z0), Point3(x1,y1,z0), Point3(x0,y1,z0), shade(color,0.9f))
        face(Point3(x0,y0,z1), Point3(x1,y0,z1), Point3(x1,y1,z1), Point3(x0,y1,z1), shade(color,0.8f))
        face(Point3(x0,y0,z0), Point3(x0,y0,z1), Point3(x0,y1,z1), Point3(x0,y1,z0), shade(color,0.7f))
        face(Point3(x1,y0,z0), Point3(x1,y0,z1), Point3(x1,y1,z1), Point3(x1,y1,z0), shade(color,0.78f))
        face(Point3(x0,y1,z0), Point3(x1,y1,z0), Point3(x1,y1,z1), Point3(x0,y1,z1), shade(color,1.08f))
        face(Point3(x0,y0,z0), Point3(x1,y0,z0), Point3(x1,y0,z1), Point3(x0,y0,z1), shade(color,0.6f))
    }

    fun roundedBack(x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, color: Int) {
        val r = minOf(0.14f, width / 4f)
        val profile = mutableListOf(x to y, x + width to y)
        for (i in 0..4) {
            val a = Math.toRadians((i * 22.5)).toFloat()
            profile.add(x + width - r + cos(a) * r to y + height - r + sin(a) * r)
        }
        for (i in 0..4) {
            val a = Math.toRadians(90.0 + i * 22.5).toFloat()
            profile.add(x + r + cos(a) * r to y + height - r + sin(a) * r)
        }
        for (i in profile.indices) {
            val a = profile[i]
            val b = profile[(i + 1) % profile.size]
            vertex(Point3(x + width / 2f, y + height / 2f, z), color)
            vertex(Point3(a.first, a.second, z), color)
            vertex(Point3(b.first, b.second, z), color)
            vertex(Point3(x + width / 2f, y + height / 2f, z + depth), shade(color, 0.85f))
            vertex(Point3(b.first, b.second, z + depth), shade(color, 0.85f))
            vertex(Point3(a.first, a.second, z + depth), shade(color, 0.85f))
            face(Point3(a.first,a.second,z), Point3(b.first,b.second,z),
                Point3(b.first,b.second,z+depth), Point3(a.first,a.second,z+depth), shade(color,0.75f))
        }
    }

    fun buffer(): FloatBuffer = ByteBuffer.allocateDirect(values.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(values.toFloatArray()); position(0) }
}

private fun shade(color: Int, factor: Float): Int {
    fun channel(shift: Int) = (((color shr shift and 255) * factor).toInt().coerceIn(0, 255)) shl shift
    return channel(16) or channel(8) or channel(0)
}
