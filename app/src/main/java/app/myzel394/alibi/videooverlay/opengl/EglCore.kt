package app.myzel394.alibi.videooverlay.opengl

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.view.Surface

private const val EGL_RECORDABLE_ANDROID = 0x3142

class EglCore {
    private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var config: EGLConfig? = null
    private var context: EGLContext = EGL14.EGL_NO_CONTEXT

    fun init(sharedContext: EGLContext = EGL14.EGL_NO_CONTEXT) {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY).also {
            check(it != EGL14.EGL_NO_DISPLAY) {
                "eglGetDisplay failed: ${EGL14.eglGetError()}"
            }
        }

        val version = IntArray(2)
        check(EGL14.eglInitialize(display, version, 0, version, 1)) {
            "eglInitialize failed: ${EGL14.eglGetError()}"
        }

        val attributes = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT or EGL14.EGL_PBUFFER_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE,
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        check(
            EGL14.eglChooseConfig(
                display,
                attributes,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0,
            )
        ) {
            "eglChooseConfig failed: ${EGL14.eglGetError()}"
        }
        config = configs[0]

        val contextAttributes = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE,
        )
        context = EGL14.eglCreateContext(
            display,
            config,
            sharedContext,
            contextAttributes,
            0,
        ).also {
            check(it != EGL14.EGL_NO_CONTEXT) {
                "eglCreateContext failed: ${EGL14.eglGetError()}"
            }
        }
    }

    fun createPbufferSurface(width: Int, height: Int): EGLSurface {
        val attributes = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE,
        )

        return EGL14.eglCreatePbufferSurface(display, config, attributes, 0).also {
            check(it != EGL14.EGL_NO_SURFACE) {
                "eglCreatePbufferSurface failed: ${EGL14.eglGetError()}"
            }
        }
    }

    fun createWindowSurface(surface: Surface): EGLSurface {
        val attributes = intArrayOf(EGL14.EGL_NONE)

        return EGL14.eglCreateWindowSurface(display, config, surface, attributes, 0).also {
            check(it != EGL14.EGL_NO_SURFACE) {
                "eglCreateWindowSurface failed: ${EGL14.eglGetError()}"
            }
        }
    }

    fun makeCurrent(surface: EGLSurface) {
        check(EGL14.eglMakeCurrent(display, surface, surface, context)) {
            "eglMakeCurrent failed: ${EGL14.eglGetError()}"
        }
    }

    fun setPresentationTime(surface: EGLSurface, timestampNs: Long) {
        check(EGLExt.eglPresentationTimeANDROID(display, surface, timestampNs)) {
            "eglPresentationTimeANDROID failed: ${EGL14.eglGetError()}"
        }
    }

    fun swapBuffers(surface: EGLSurface) {
        check(EGL14.eglSwapBuffers(display, surface)) {
            "eglSwapBuffers failed: ${EGL14.eglGetError()}"
        }
    }

    fun destroySurface(surface: EGLSurface) {
        if (surface == EGL14.EGL_NO_SURFACE) {
            return
        }

        check(EGL14.eglDestroySurface(display, surface)) {
            "eglDestroySurface failed: ${EGL14.eglGetError()}"
        }
    }

    fun release() {
        if (display == EGL14.EGL_NO_DISPLAY) {
            return
        }

        EGL14.eglMakeCurrent(
            display,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT,
        )

        if (context != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(display, context)
            context = EGL14.EGL_NO_CONTEXT
        }

        EGL14.eglReleaseThread()
        EGL14.eglTerminate(display)
        display = EGL14.EGL_NO_DISPLAY
        config = null
    }
}
