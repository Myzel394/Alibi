package app.myzel394.alibi.helpers

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CompletableDeferred

class AppLockHelper {
    enum class SupportType {
        AVAILABLE,
        UNAVAILABLE,
        NONE_ENROLLED,
    }

    companion object {
        fun isSupported(context: Context): SupportType {
            val biometricManager = BiometricManager.from(context)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> return SupportType.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return SupportType.NONE_ENROLLED

                else -> return SupportType.UNAVAILABLE
            }
        }

        fun authenticate(
            context: Context,
            title: String,
            subtitle: String
        ): CompletableDeferred<Unit> {
            val deferred = CompletableDeferred<Unit>()

            val mainExecutor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                context as FragmentActivity,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        deferred.completeExceptionally(Exception(errString.toString()))
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        deferred.complete(Unit)
                    }

                    override fun onAuthenticationFailed() {
                        deferred.completeExceptionally(Exception("Authentication failed"))
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
    }
}
