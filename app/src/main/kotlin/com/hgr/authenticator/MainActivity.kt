package com.hgr.authenticator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.hgr.authenticator.domain.model.Settings
import com.hgr.authenticator.domain.usecase.GetSettingsUseCase
import com.hgr.authenticator.presentation.common.BiometricLockScreen
import com.hgr.authenticator.presentation.navigation.AppNavigation
import com.hgr.authenticator.presentation.theme.AuthenticatorTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var getSettingsUseCase: GetSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by getSettingsUseCase().collectAsState(initial = Settings())

            AuthenticatorTheme(themeMode = settings.themeMode) {
                BiometricLockScreen(biometricEnabled = settings.biometricEnabled) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
