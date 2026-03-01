package com.ghostkey.ui.theme

import androidx.compose.ui.graphics.Color

// GhostKey dark theme palette
val Background = Color(0xFF0D1117)
val Surface = Color(0xFF161B22)
val SurfaceVariant = Color(0xFF21262D)
val OnBackground = Color(0xFFE6EDF3)
val OnSurface = Color(0xFFE6EDF3)
val OnSurfaceVariant = Color(0xFF8B949E)

val Cyan = Color(0xFF58A6FF)       // active style indicator, primary accent
val Amber = Color(0xFFF59E0B)      // warnings, flags
val KeyBorder = Color(0xFF30363D)
val AliasBar = Color(0xFF010409)
val AmoledBlack = Color(0xFF000000)

val GhostKeyDarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Cyan,
    onPrimary = Background,
    secondary = Amber,
    onSecondary = Background,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = KeyBorder,
    error = Color(0xFFFF7B72)
)
