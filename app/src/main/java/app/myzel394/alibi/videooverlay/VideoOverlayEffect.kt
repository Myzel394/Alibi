package app.myzel394.alibi.videooverlay

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import androidx.camera.core.CameraEffect
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceProcessor
import androidx.camera.core.SurfaceRequest
import androidx.core.util.Consumer
import app.myzel394.alibi.videooverlay.opengl.OpenGlRenderer
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class VideoOverlayEffect(
    targets: Int,
    private val surfaceProcessor: VideoOverlaySurfaceProcessor,
    errorListener: Consumer<Throwable>,
) : CameraEffect(
    targets,
    surfaceProcessor.glExecutor,
    surfaceProcessor,
    errorListener,
) {
    private val released = AtomicBoolean(false)

    constructor(
        overlayTextProvider: () -> String,
        errorListener: Consumer<Throwable>,
        targets: Int = VIDEO_CAPTURE,
    ) : this(
        targets,
        VideoOverlaySurfaceProcessor(overlayTextProvider, errorListener),
        errorListener,
    )

    fun release() {
        if (released.getAndSet(true)) {
            return
        }

        surfaceProcessor.release()
    }
}

class VideoOverlaySurfaceProcessor(
    overlayTextProvider: () -> String,
    private val errorListener: Consumer<Throwable>,
) : SurfaceProcessor, SurfaceTexture.OnFrameAvailableListener {
    private val glThread: HandlerThread = HandlerThread("VideoOverlayGlThread").apply {
        start()
    }
    private val glHandler = Handler(glThread.looper)
    private val openGlRenderer = OpenGlRenderer(overlayTextProvider)
    private val texMatrix = FloatArray(16)

    private var inputSurface: InputSurface? = null

    val glExecutor: Executor = Executor { command ->
        glHandler.post(command)
    }

    private data class InputSurface(
        val surfaceTexture: SurfaceTexture,
        val surface: Surface,
    ) {
        fun release() {
            surfaceTexture.setOnFrameAvailableListener(null)
            surfaceTexture.release()
            surface.release()
        }
    }

    init {
        glHandler.post {
            try {
                openGlRenderer.init()
            } catch (error: RuntimeException) {
                errorListener.accept(error)
            }
        }
    }

    override fun onInputSurface(request: SurfaceRequest) {
        val surfaceTexture = SurfaceTexture(openGlRenderer.textureId).apply {
            setOnFrameAvailableListener(this@VideoOverlaySurfaceProcessor, glHandler)
            setDefaultBufferSize(request.resolution.width, request.resolution.height)
        }
        val surface = Surface(surfaceTexture)
        val newInputSurface = InputSurface(surfaceTexture, surface)
        inputSurface = newInputSurface

        request.provideSurface(surface, glExecutor) {
            newInputSurface.release()
            if (inputSurface == newInputSurface) {
                inputSurface = null
            }
        }
    }

    override fun onOutputSurface(surfaceOutput: SurfaceOutput) {
        val surface = surfaceOutput.getSurface(glExecutor) {
            openGlRenderer.unregister(surfaceOutput)
            surfaceOutput.close()
        }
        openGlRenderer.register(surfaceOutput, surface)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        try {
            surfaceTexture.updateTexImage()
            surfaceTexture.getTransformMatrix(texMatrix)
            openGlRenderer.draw(surfaceTexture.timestamp, texMatrix)
        } catch (error: RuntimeException) {
            errorListener.accept(error)
        }
    }

    fun release() {
        glHandler.post {
            inputSurface?.release()
            inputSurface = null
            openGlRenderer.release()
            glThread.quitSafely()
        }
    }
}
