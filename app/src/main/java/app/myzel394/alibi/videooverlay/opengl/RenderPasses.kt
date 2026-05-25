package app.myzel394.alibi.videooverlay.opengl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Size
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

const val GL_INVALID = -1

private const val FLOAT_SIZE_BYTES = 4

private val VERTEX_COORDS = floatArrayOf(
    -1f, -1f,
    1f, -1f,
    -1f, 1f,
    1f, 1f,
).toFloatBuffer()

private val TEX_COORDS = floatArrayOf(
    0f, 0f,
    1f, 0f,
    0f, 1f,
    1f, 1f,
).toFloatBuffer()

private const val VERTEX_SHADER_SOURCE = """
attribute vec4 aPosition;
attribute vec4 aTexCoords;
uniform mat4 uMvpMatrix;
uniform mat4 uTexMatrix;
varying vec2 vTexCoords;

void main() {
    vTexCoords = (uTexMatrix * aTexCoords).xy;
    gl_Position = uMvpMatrix * aPosition;
}
"""

private const val EXTERNAL_TEXTURE_FRAGMENT_SHADER_SOURCE = """
#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES uTexture;
varying vec2 vTexCoords;

void main() {
    gl_FragColor = texture2D(uTexture, vTexCoords);
}
"""

private const val TEXTURE_2D_FRAGMENT_SHADER_SOURCE = """
precision mediump float;

uniform sampler2D uTexture;
varying vec2 vTexCoords;

void main() {
    gl_FragColor = texture2D(uTexture, vec2(vTexCoords.x, 1.0 - vTexCoords.y));
}
"""

interface RenderPass {
    fun init()

    fun release()
}

class CameraRenderPass(
    private val program: TextureProgram = TextureProgram(EXTERNAL_TEXTURE_FRAGMENT_SHADER_SOURCE),
) : RenderPass {
    var textureId = GL_INVALID
        private set

    override fun init() {
        program.init()
        textureId = createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
    }

    fun draw(texMatrix: FloatArray, mvpMatrix: FloatArray, surfaceSize: Size) {
        GLES20.glViewport(0, 0, surfaceSize.width, surfaceSize.height)
        checkGlError("camera viewport")
        program.draw(
            textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            textureId = textureId,
            texMatrix = texMatrix,
            mvpMatrix = mvpMatrix,
        )
    }

    override fun release() {
        if (textureId != GL_INVALID) {
            GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = GL_INVALID
        }
        program.release()
    }
}

class OverlayRenderPass(
    private val overlayTextProvider: () -> String,
    private val program: TextureProgram = TextureProgram(TEXTURE_2D_FRAGMENT_SHADER_SOURCE),
) : RenderPass {
    private val paint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        color = Color.WHITE
    }
    private var textureId = GL_INVALID
    private var bitmap: Bitmap? = null
    private var lastText: String? = null
    private var lastSurfaceHeight: Int? = null
    private var textureDirty = true

    override fun init() {
        program.init()
        textureId = createTexture(GLES20.GL_TEXTURE_2D)
    }

    fun draw(texMatrix: FloatArray, mvpMatrix: FloatArray, surfaceSize: Size) {
        val text = overlayTextProvider()
        if (text.isBlank()) {
            return
        }

        val overlayBitmap = getBitmap(text, surfaceSize)
        if (textureDirty) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            checkGlError("bind overlay texture")
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, overlayBitmap, 0)
            checkGlError("upload overlay texture")
            textureDirty = false
        }

        val padding = (surfaceSize.height * 0.02f).roundToInt().coerceAtLeast(12)
        val maxWidth = (surfaceSize.width - padding * 2).coerceAtLeast(1)
        val scale = min(1f, maxWidth / overlayBitmap.width.toFloat())
        val viewportWidth = (overlayBitmap.width * scale).roundToInt().coerceAtLeast(1)
        val viewportHeight = (overlayBitmap.height * scale).roundToInt().coerceAtLeast(1)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glViewport(padding, padding, viewportWidth, viewportHeight)
        checkGlError("overlay viewport")

        program.draw(
            textureTarget = GLES20.GL_TEXTURE_2D,
            textureId = textureId,
            texMatrix = texMatrix,
            mvpMatrix = mvpMatrix,
        )

        GLES20.glDisable(GLES20.GL_BLEND)
        checkGlError("disable overlay blend")
    }

    private fun getBitmap(text: String, surfaceSize: Size): Bitmap {
        if (text == lastText && lastSurfaceHeight == surfaceSize.height && bitmap != null) {
            return bitmap!!
        }

        val textSize = max(18f, surfaceSize.height * 0.035f)
        paint.textSize = textSize
        paint.setShadowLayer(max(2f, textSize / 12f), 0f, 0f, Color.BLACK)

        val fontMetrics = paint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * 1.15f
        val innerPadding = (textSize * 0.25f).roundToInt()
        val lines = text.lines()
        val width = (
                lines.maxOf { line -> paint.measureText(line) } + innerPadding * 2
                ).roundToInt().coerceAtLeast(1)
        val height = (lineHeight * lines.size + innerPadding * 2).roundToInt().coerceAtLeast(1)

        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        lines.forEachIndexed { index, line ->
            val baseline = innerPadding - fontMetrics.ascent + index * lineHeight
            canvas.drawText(line, innerPadding.toFloat(), baseline, paint)
        }

        bitmap?.recycle()
        bitmap = newBitmap
        lastText = text
        lastSurfaceHeight = surfaceSize.height
        textureDirty = true

        return newBitmap
    }

    override fun release() {
        if (textureId != GL_INVALID) {
            GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = GL_INVALID
        }
        bitmap?.recycle()
        bitmap = null
        program.release()
    }
}

class TextureProgram(
    private val fragmentShaderSource: String,
) {
    private var programId = GL_INVALID
    private var vertexShaderId = GL_INVALID
    private var fragmentShaderId = GL_INVALID
    private var aPosition = GL_INVALID
    private var aTexCoords = GL_INVALID
    private var uMvpMatrix = GL_INVALID
    private var uTexMatrix = GL_INVALID
    private var uTexture = GL_INVALID

    fun init() {
        vertexShaderId = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_SOURCE)
        fragmentShaderId = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
        programId = GLES20.glCreateProgram()
        check(programId != GL_INVALID) {
            "glCreateProgram failed"
        }
        checkGlError("create program")

        GLES20.glAttachShader(programId, vertexShaderId)
        GLES20.glAttachShader(programId, fragmentShaderId)
        GLES20.glLinkProgram(programId)
        checkProgramLinkStatus(programId)

        aPosition = GLES20.glGetAttribLocation(programId, "aPosition")
        aTexCoords = GLES20.glGetAttribLocation(programId, "aTexCoords")
        uMvpMatrix = GLES20.glGetUniformLocation(programId, "uMvpMatrix")
        uTexMatrix = GLES20.glGetUniformLocation(programId, "uTexMatrix")
        uTexture = GLES20.glGetUniformLocation(programId, "uTexture")
        checkGlError("get program locations")
    }

    fun draw(
        textureTarget: Int,
        textureId: Int,
        texMatrix: FloatArray,
        mvpMatrix: FloatArray,
    ) {
        GLES20.glUseProgram(programId)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(textureTarget, textureId)
        GLES20.glUniform1i(uTexture, 0)
        GLES20.glUniformMatrix4fv(uTexMatrix, 1, false, texMatrix, 0)
        GLES20.glUniformMatrix4fv(uMvpMatrix, 1, false, mvpMatrix, 0)
        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glVertexAttribPointer(
            aPosition,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            VERTEX_COORDS,
        )
        GLES20.glEnableVertexAttribArray(aTexCoords)
        GLES20.glVertexAttribPointer(
            aTexCoords,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            TEX_COORDS,
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(aPosition)
        GLES20.glDisableVertexAttribArray(aTexCoords)
        GLES20.glBindTexture(textureTarget, 0)
        GLES20.glUseProgram(0)
        checkGlError("draw texture")
    }

    fun release() {
        if (programId != GL_INVALID) {
            GLES20.glDeleteProgram(programId)
            programId = GL_INVALID
        }
        if (vertexShaderId != GL_INVALID) {
            GLES20.glDeleteShader(vertexShaderId)
            vertexShaderId = GL_INVALID
        }
        if (fragmentShaderId != GL_INVALID) {
            GLES20.glDeleteShader(fragmentShaderId)
            fragmentShaderId = GL_INVALID
        }
    }
}

private fun createTexture(textureTarget: Int): Int {
    val textureIds = IntArray(1)
    GLES20.glGenTextures(1, textureIds, 0)
    GLES20.glBindTexture(textureTarget, textureIds[0])
    GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    checkGlError("create texture")
    return textureIds[0]
}

private fun loadShader(shaderType: Int, source: String): Int {
    val shaderId = GLES20.glCreateShader(shaderType)
    check(shaderId != GL_INVALID) {
        "glCreateShader failed"
    }
    GLES20.glShaderSource(shaderId, source)
    GLES20.glCompileShader(shaderId)
    checkShaderCompileStatus(shaderId)
    return shaderId
}

private fun checkShaderCompileStatus(shaderId: Int) {
    val status = IntArray(1)
    GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, status, 0)
    check(status[0] == GLES20.GL_TRUE) {
        "Shader compilation failed: ${GLES20.glGetShaderInfoLog(shaderId)}"
    }
}

private fun checkProgramLinkStatus(programId: Int) {
    val status = IntArray(1)
    GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, status, 0)
    check(status[0] == GLES20.GL_TRUE) {
        "Program linking failed: ${GLES20.glGetProgramInfoLog(programId)}"
    }
}

private fun checkGlError(operation: String) {
    val error = GLES20.glGetError()
    check(error == GLES20.GL_NO_ERROR) {
        "$operation failed with GL error $error"
    }
}

private fun FloatArray.toFloatBuffer(): FloatBuffer {
    val buffer = ByteBuffer
        .allocateDirect(size * FLOAT_SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    buffer.put(this)
    buffer.position(0)
    return buffer
}
