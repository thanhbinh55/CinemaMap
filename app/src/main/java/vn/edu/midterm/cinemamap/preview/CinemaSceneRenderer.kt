package vn.edu.midterm.cinemamap.preview

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.atan
import kotlin.math.tan

internal class CinemaSceneRenderer(
    private val geometry: CinemaSceneGeometry,
    private val mesh: CinemaSceneMesh,
    initialCamera: SeatCameraState,
    private val requestFrame: () -> Unit,
    private val onVideoSurface: (Surface) -> Unit,
    private val onVideoError: (Exception) -> Unit,
    private val onReady: () -> Unit,
    private val onError: () -> Unit
) : GLSurfaceView.Renderer {
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var matrixHandle = 0
    private var lightingHandle = 0
    private var reportedReady = false
    private lateinit var roomVertices: FloatBuffer
    private lateinit var chairVertices: FloatBuffer
    private lateinit var shadowVertices: FloatBuffer
    private var camera = initialCamera
    private var videoScreen: CinemaVideoScreen? = null
    private var announceVideoSurface = true

    fun requestVideoSurface() {
        announceVideoSurface = true
    }

    fun releaseVideoSurface() {
        videoScreen?.releaseSurface()
        videoScreen = null
    }

    // Called only on the GL thread via GLSurfaceView.queueEvent().
    fun updateCamera(state: SeatCameraState) {
        val changedSeat = state.seatId != camera.seatId
        camera = state
        if (program == 0) return
        if (changedSeat) {
            chairVertices = mesh.chairs(camera.seatId)
            shadowVertices = mesh.shadows(camera.seatId)
        }
        updateViewMatrix()
    }

    private fun updateViewMatrix() {
        val eye = geometry.eyeAt(camera.seatId, camera.side)
        val direction = geometry.direction(camera)
        Matrix.setLookAtM(viewMatrix, 0, eye.x, eye.y, eye.z,
            eye.x + direction.x, eye.y + direction.y, eye.z + direction.z, 0f, 1f, 0f)
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // This is called again after an EGL context is lost; recreate GPU resources.
        program = 0
        reportedReady = false
        releaseVideoSurface()
        announceVideoSurface = true
        try {
            program = createProgram()
            positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
            colorHandle = GLES20.glGetAttribLocation(program, "aColor")
            matrixHandle = GLES20.glGetUniformLocation(program, "uViewProjection")
            lightingHandle = GLES20.glGetUniformLocation(program, "uLighting")
            roomVertices = mesh.room()
            chairVertices = mesh.chairs(camera.seatId)
            shadowVertices = mesh.shadows(camera.seatId)
            GLES20.glClearColor(16f / 255f, 25f / 255f, 35f / 255f, 1f)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            updateViewMatrix()
            try {
                videoScreen = CinemaVideoScreen(geometry, requestFrame)
            } catch (error: RuntimeException) {
                Log.e("CinemaPreview", "Cannot initialize the video screen", error)
                onVideoError(error)
            }
        } catch (error: IllegalStateException) {
            Log.e("CinemaPreview", "Cannot initialize the 3D scene", error)
            program = 0
            onError()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.coerceAtLeast(1).toFloat() / height.coerceAtLeast(1)
        // Same horizontal FOV for every seat/orientation, without zoom distorting comparisons.
        val verticalFov = Math.toDegrees(2.0 * atan(tan(Math.toRadians(geometry.spec.horizontalFovDegrees / 2.0)) / aspect)).toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, verticalFov, aspect, 0.08f, 64f)
        updateViewMatrix()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (program == 0) return
        if (announceVideoSurface) {
            videoScreen?.let { onVideoSurface(it.surface) }
            announceVideoSurface = false
        }
        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, viewProjectionMatrix, 0)
        GLES20.glUniform1f(lightingHandle, if (geometry.spec.enhancedLighting) 1f else 0f)
        draw(roomVertices, GLES20.GL_TRIANGLES)
        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL)
        GLES20.glPolygonOffset(-1f, -1f)
        draw(shadowVertices, GLES20.GL_TRIANGLES)
        GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL)
        try {
            videoScreen?.draw(viewProjectionMatrix)
            val videoError = GLES20.glGetError()
            check(videoError == GLES20.GL_NO_ERROR) { "Video GL error: $videoError" }
        } catch (error: RuntimeException) {
            Log.e("CinemaPreview", "Cannot draw the trailer; retaining the fallback screen", error)
            releaseVideoSurface()
            onVideoError(error)
        }
        GLES20.glUseProgram(program)
        draw(chairVertices, GLES20.GL_TRIANGLES)
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("CinemaPreview", "OpenGL draw error: $error")
            program = 0
            onError()
        } else if (!reportedReady) {
            reportedReady = true
            onReady()
        }
    }

    private fun draw(vertices: FloatBuffer, mode: Int) {
        if (vertices.capacity() == 0) return
        val stride = 6 * Float.SIZE_BYTES // XYZ followed by RGB for each vertex.
        vertices.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, stride, vertices)
        vertices.position(3)
        GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_FLOAT, false, stride, vertices)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glDrawArrays(mode, 0, vertices.capacity() / 6)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    private fun createProgram(): Int = createGlProgram("""
            uniform mat4 uViewProjection;
            uniform float uLighting;
            attribute vec3 aPosition;
            attribute vec3 aColor;
            varying vec3 vColor;
            void main() {
                gl_Position = uViewProjection * vec4(aPosition, 1.0);
                float glow = 1.0 / (1.0 + max(aPosition.z, 0.0) * 0.12);
                vec3 lit = aColor * (0.87 + 0.13 * glow) + vec3(0.025, 0.04, 0.06) * glow;
                vColor = clamp(mix(aColor, lit, uLighting), 0.0, 1.0);
            }
        """.trimIndent(), """
                precision mediump float;
                varying vec3 vColor;
                void main() { gl_FragColor = vec4(vColor, 1.0); }
            """.trimIndent())
}
