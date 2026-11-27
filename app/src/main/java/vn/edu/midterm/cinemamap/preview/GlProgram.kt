package vn.edu.midterm.cinemamap.preview

import android.opengl.GLES20

internal fun createGlProgram(vertexSource: String, fragmentSource: String): Int {
    val vertex = compileGlShader(GLES20.GL_VERTEX_SHADER, vertexSource)
    val fragment = try {
        compileGlShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
    } catch (error: IllegalStateException) {
        GLES20.glDeleteShader(vertex)
        throw error
    }
    val result = GLES20.glCreateProgram()
    GLES20.glAttachShader(result, vertex)
    GLES20.glAttachShader(result, fragment)
    GLES20.glLinkProgram(result)
    GLES20.glDeleteShader(vertex)
    GLES20.glDeleteShader(fragment)
    val linked = IntArray(1)
    GLES20.glGetProgramiv(result, GLES20.GL_LINK_STATUS, linked, 0)
    if (linked[0] == 0) {
        val message = GLES20.glGetProgramInfoLog(result)
        GLES20.glDeleteProgram(result)
        error("Program link failed: $message")
    }
    return result
}

private fun compileGlShader(type: Int, source: String): Int {
    val shader = GLES20.glCreateShader(type)
    GLES20.glShaderSource(shader, source)
    GLES20.glCompileShader(shader)
    val compiled = IntArray(1)
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
        val message = GLES20.glGetShaderInfoLog(shader)
        GLES20.glDeleteShader(shader)
        error("Shader compilation failed: $message")
    }
    return shader
}
