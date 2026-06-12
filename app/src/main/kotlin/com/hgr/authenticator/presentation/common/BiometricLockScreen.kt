package com.hgr.authenticator.presentation.common

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.hgr.authenticator.R

@Composable
fun BiometricLockScreen(
    biometricEnabled: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = remember { context as? FragmentActivity }
    var isUnlocked by remember { mutableStateOf(false) }
    var showRetry by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val authTitle = stringResource(R.string.biometric_auth_title)
    val authSubtitle = stringResource(R.string.biometric_auth_subtitle)
    val cancelLabel = stringResource(R.string.cancel)
    val unsupportedMsg = stringResource(R.string.lock_screen_unsupported)
    val retryMsg = stringResource(R.string.lock_screen_retry)
    val retryAction = stringResource(R.string.lock_screen_retry_action)
    val lockTitle = stringResource(R.string.lock_screen_title)
    val lockDesc = stringResource(R.string.lock_screen_desc)

    fun showBiometricPrompt() {
        val act = activity ?: return
        val canAuthenticate = BiometricManager.from(act)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            showRetry = true
            return
        }
        val executor = ContextCompat.getMainExecutor(act)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(authTitle)
            .setSubtitle(authSubtitle)
            .setNegativeButtonText(cancelLabel)
            .build()
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                isUnlocked = true
                showRetry = false
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
                    errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE
                ) {
                    showRetry = true
                }
            }

            override fun onAuthenticationFailed() {
                showRetry = true
            }
        }
        BiometricPrompt(act, executor, callback).authenticate(promptInfo)
    }

    LaunchedEffect(biometricEnabled) {
        isLoading = true
        showRetry = false
        if (biometricEnabled) {
            isUnlocked = false
            showBiometricPrompt()
        } else {
            isUnlocked = true
        }
        isLoading = false
    }

    if (isUnlocked) {
        content()
    } else {
        LockScreen(
            showRetry = showRetry,
            canRetry = activity != null,
            onRetry = { showBiometricPrompt() }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LockScreenPreview() {
    LockScreen(showRetry = false, canRetry = true, onRetry = {})
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LockScreenRetryPreview() {
    LockScreen(showRetry = true, canRetry = true, onRetry = {})
}

@Composable
private fun LockScreen(
    showRetry: Boolean,
    canRetry: Boolean,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.lock_screen_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.lock_screen_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!canRetry) {
                Text(
                    text = stringResource(R.string.lock_screen_unsupported),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else if (showRetry) {
                Text(
                    text = stringResource(R.string.lock_screen_retry),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (canRetry) {
                Button(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.lock_screen_retry_action))
                }
            }
        }
    }
}
