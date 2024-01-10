package app.myzel394.alibi.helpers

import android.app.Activity
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CompletableDeferred
import kotlin.system.exitProcess

class AppLockHelper {
    enum class SupportType {
        AVAILABLE,
        UNAVAILABLE,
        NONE_ENROLLED,
    }

    companion object {
        fun getSupportType(context: Context): SupportType {
            val biometricManager = BiometricManager.from(context)
            return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> SupportType.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> SupportType.NONE_ENROLLED

                else -> SupportType.UNAVAILABLE
            }
        }

        fun authenticate(
            context: Context,
            title: String,
            subtitle: String
        ): CompletableDeferred<Boolean> {
            val deferred = CompletableDeferred<Boolean>()

            val mainExecutor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                context as FragmentActivity,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        deferred.complete(false)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        deferred.complete(true)
                    }

                    override fun onAuthenticationFailed() {
                        deferred.complete(false)
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)

            return deferred
        }

        fun closeApp(context: Context) {
            (context as? Activity)?.let {
                it.finishAndRemoveTask()
                it.finishAffinity()
                it.finish()
            }

            exitProcess(0)
        }
    }
}
