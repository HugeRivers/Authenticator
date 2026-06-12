package com.hgr.authenticator.presentation.theme

import androidx.compose.ui.graphics.Color

// Light Theme
val LightBackground = Color(0xFFF5F4ED)
val LightSurface = Color(0xFFFAF9F5)
val LightSurfaceWarm = Color(0xFFE8E6DC)
val LightForeground = Color(0xFF141413)
val LightForeground2 = Color(0xFF3D3D3A)
val LightMuted = Color(0xFF5E5D59)
val LightMeta = Color(0xFF87867F)
val LightBorder = Color(0xFFF0EEE6)
val LightBorderSoft = Color(0xFFE8E6DC)
val LightAccent = Color(0xFFC96442)
val LightAccentOn = Color(0xFFFAF9F5)
val LightSuccess = Color(0xFF17A34A)
val LightWarn = Color(0xFFEAB308)
val LightDanger = Color(0xFFB53333)

// Dark Theme
val DarkBackground = Color(0xFF141413)
val DarkSurface = Color(0xFF1E1E1C)
val DarkSurfaceWarm = Color(0xFF2A2A28)
val DarkForeground = Color(0xFFFAF9F5)
val DarkForeground2 = Color(0xFFE8E6DC)
val DarkMuted = Color(0xFFB0AEA5)
val DarkMeta = Color(0xFF87867F)
val DarkBorder = Color(0xFF30302E)
val DarkBorderSoft = Color(0xFF3D3D3A)
val DarkAccent = Color(0xFFC96442)
val DarkAccentOn = Color(0xFFFAF9F5)
val DarkSuccess = Color(0xFF17A34A)
val DarkWarn = Color(0xFFEAB308)
val DarkDanger = Color(0xFFB53333)

// Brand Colors for Account Icons
val BrandGoogle = Color(0xFF4285F4)
val BrandGitHub = Color(0xFF333333)
val BrandMicrosoft = Color(0xFF00A4EF)
val BrandDropbox = Color(0xFF0061FF)
val BrandSlack = Color(0xFF4A154B)
val BrandAWS = Color(0xFFFF9900)
val BrandDefault = LightAccent

fun getBrandColor(issuer: String): Color {
    return when (issuer.lowercase()) {
        "google" -> BrandGoogle
        "github" -> BrandGitHub
        "microsoft" -> BrandMicrosoft
        "dropbox" -> BrandDropbox
        "slack" -> BrandSlack
        "aws", "amazon" -> BrandAWS
        else -> LightAccent
    }
}
