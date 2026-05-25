package app.myzel394.alibi.videooverlay.opengl

import android.opengl.EGL14
import android.opengl.EGLSurface
import android.opengl.Matrix
import android.view.Surface
import androidx.camera.core.SurfaceOutput

class OpenGlRenderer(
    overlayTextProvider: () -> String,
    private val eglCore: EglCore = EglCore(),
    private val cameraRenderPass: CameraRenderPass = CameraRenderPass(),
    private val overlayRenderPass: OverlayRenderPass = OverlayRenderPass(overlayTextProvider),
) {
    private val surfaceMap = HashMap<SurfaceOutput, EGLSurface>()
    private val identityMatrix = FloatArray(16).also {
        Matrix.setIdentityM(it, 0)
    }
    private val outputTexMatrix = FloatArray(16)
    private var tempSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var initialized = false

    val textureId: Int
        get() = cameraRenderPass.textureId.also {
            check(it != GL_INVALID) {
                "OpenGL renderer texture ID is not initialized"
            }
        }

    fun init() {
        if (initialized) {
            return
        }

        eglCore.init()
        tempSurface = eglCore.createPbufferSurface(1, 1)
        eglCore.makeCurrent(tempSurface)

        cameraRenderPass.init()
        overlayRenderPass.init()
        initialized = true
    }

    fun register(surfaceOutput: SurfaceOutput, surface: Surface) {
        surfaceMap.getOrPut(surfaceOutput) {
            eglCore.createWindowSurface(surface)
        }
    }

    fun unregister(surfaceOutput: SurfaceOutput) {
        surfaceMap.remove(surfaceOutput)?.let {
            eglCore.destroySurface(it)
        }
    }

    fun draw(timestampNs: Long, texMatrix: FloatArray) {
        surfaceMap.forEach { (surfaceOutput, eglSurface) ->
            eglCore.makeCurrent(eglSurface)
            surfaceOutput.updateTransformMatrix(outputTexMatrix, texMatrix)
            cameraRenderPass.draw(outputTexMatrix, identityMatrix, surfaceOutput.size)
            overlayRenderPass.draw(identityMatrix, identityMatrix, surfaceOutput.size)
            eglCore.setPresentationTime(eglSurface, timestampNs)
            eglCore.swapBuffers(eglSurface)
        }
    }

    fun release() {
        if (!initialized) {
            return
        }

        surfaceMap.forEach { (surfaceOutput, eglSurface) ->
            surfaceOutput.close()
            eglCore.destroySurface(eglSurface)
        }
        surfaceMap.clear()

        cameraRenderPass.release()
        overlayRenderPass.release()
        eglCore.destroySurface(tempSurface)
        tempSurface = EGL14.EGL_NO_SURFACE
        eglCore.release()
        initialized = false
    }
}
