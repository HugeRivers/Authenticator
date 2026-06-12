package com.hgr.authenticator.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.hgr.authenticator.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightAccentOn,
    primaryContainer = LightAccent.copy(alpha = 0.12f),
    onPrimaryContainer = LightAccent,
    secondary = LightSurfaceWarm,
    onSecondary = LightForeground,
    secondaryContainer = LightSurfaceWarm,
    onSecondaryContainer = LightForeground,
    tertiary = LightSuccess,
    onTertiary = LightAccentOn,
    tertiaryContainer = LightSuccess.copy(alpha = 0.12f),
    onTertiaryContainer = LightSuccess,
    background = LightBackground,
    onBackground = LightForeground,
    surface = LightSurface,
    onSurface = LightForeground,
    surfaceVariant = LightSurfaceWarm,
    onSurfaceVariant = LightMuted,
    surfaceTint = LightAccent,
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkForeground,
    error = LightDanger,
    onError = LightAccentOn,
    errorContainer = LightDanger.copy(alpha = 0.12f),
    onErrorContainer = LightDanger,
    outline = LightBorder,
    outlineVariant = LightBorderSoft,
    scrim = LightForeground.copy(alpha = 0.4f)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkAccentOn,
    primaryContainer = DarkAccent.copy(alpha = 0.15f),
    onPrimaryContainer = DarkAccent,
    secondary = DarkSurfaceWarm,
    onSecondary = DarkForeground,
    secondaryContainer = DarkSurfaceWarm,
    onSecondaryContainer = DarkForeground,
    tertiary = DarkSuccess,
    onTertiary = DarkAccentOn,
    tertiaryContainer = DarkSuccess.copy(alpha = 0.15f),
    onTertiaryContainer = DarkSuccess,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkSurface,
    onSurface = DarkForeground,
    surfaceVariant = DarkSurfaceWarm,
    onSurfaceVariant = DarkMuted,
    surfaceTint = DarkAccent,
    inverseSurface = LightSurface,
    inverseOnSurface = LightForeground,
    error = DarkDanger,
    onError = DarkAccentOn,
    errorContainer = DarkDanger.copy(alpha = 0.15f),
    onErrorContainer = DarkDanger,
    outline = DarkBorder,
    outlineVariant = DarkBorderSoft,
    scrim = DarkForeground.copy(alpha = 0.5f)
)

@Composable
fun AuthenticatorTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuthenticatorTypography,
        content = content
    )
}
