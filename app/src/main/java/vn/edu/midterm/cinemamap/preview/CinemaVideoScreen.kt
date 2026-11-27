package vn.edu.midterm.cinemamap.preview

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

/** A video texture on the real screen plane, so projection/occlusion match every seat. */
internal class CinemaVideoScreen(geometry: CinemaSceneGeometry, requestFrame: () -> Unit) {
    private val frameAvailable = AtomicBoolean(false)
    private val textureMatrix = FloatArray(16)
    private val textureId: Int
    private val program: Int
    private val positionHandle: Int
    private val uvHandle: Int
    private val viewProjectionHandle: Int
    private val textureMatrixHandle: Int
    private val samplerHandle: Int
    private val surfaceTexture: SurfaceTexture
    val surface: Surface
    private var hasFrame = false
    private val vertices = ByteBuffer.allocateDirect(4 * 5 * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            val x = geometry.screenWidth / 2f
            val y0 = geometry.screenCenter.y - geometry.screenHeight / 2f
            val y1 = geometry.screenCenter.y + geometry.screenHeight / 2f
            val z = geometry.screenCenter.z
            put(floatArrayOf(-x,y0,z,0f,0f, x,y0,z,1f,0f, -x,y1,z,0f,1f, x,y1,z,1f,1f))
            position(0)
        }

    init {
        program = createGlProgram("""
            uniform mat4 uViewProjection;
            uniform mat4 uTextureMatrix;
            attribute vec3 aPosition;
            attribute vec2 aUv;
            varying vec2 vUv;
            void main() {
                gl_Position = uViewProjection * vec4(aPosition, 1.0);
                vUv = (uTextureMatrix * vec4(aUv, 0.0, 1.0)).xy;
            }
        """.trimIndent(), """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            uniform samplerExternalOES uVideo;
            varying vec2 vUv;
            void main() { gl_FragColor = texture2D(uVideo, vUv); }
        """.trimIndent())
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        uvHandle = GLES20.glGetAttribLocation(program, "aUv")
        viewProjectionHandle = GLES20.glGetUniformLocation(program, "uViewProjection")
        textureMatrixHandle = GLES20.glGetUniformLocation(program, "uTextureMatrix")
        samplerHandle = GLES20.glGetUniformLocation(program, "uVideo")
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setOnFrameAvailableListener {
            frameAvailable.set(true)
            requestFrame()
        }
        surface = Surface(surfaceTexture)
    }

    fun draw(viewProjection: FloatArray) {
        if (frameAvailable.getAndSet(false)) {
            surfaceTexture.updateTexImage()
            surfaceTexture.getTransformMatrix(textureMatrix)
            hasFrame = true
        }
        if (!hasFrame) return
        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(viewProjectionHandle, 1, false, viewProjection, 0)
        GLES20.glUniformMatrix4fv(textureMatrixHandle, 1, false, textureMatrix, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(samplerHandle, 0)
        vertices.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 20, vertices)
        vertices.position(3)
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 20, vertices)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(uvHandle)
        // Replace the fallback at identical depth; seats are drawn afterwards with normal depth testing.
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL)
        GLES20.glPolygonOffset(-1f, -1f) // Avoid coplanar z-fighting on 16-bit depth buffers.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL)
        GLES20.glDepthFunc(GLES20.GL_LESS)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(uvHandle)
    }

    /** Java-side resources only; safe after the GL thread has exited or its context was lost. */
    fun releaseSurface() {
        surfaceTexture.setOnFrameAvailableListener(null)
        surface.release()
        surfaceTexture.release()
    }
}
