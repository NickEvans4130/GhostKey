package com.ghostkey.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun GhostKeyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GhostKeyDarkColorScheme,
        typography = GhostKeyTypography,
        content = content
    )
}
