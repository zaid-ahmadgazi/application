package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Strict 3-Color Scheme:
// - Purple for logo: PurpleLogo
// - Cream color for background: CreamBackground / CreamSurface
// - Black for border and text: BlackBorder / BlackText

private val StrictColorScheme = lightColorScheme(
    primary = BlackText,
    onPrimary = CreamBackground,
    primaryContainer = CreamSurfaceVariant,
    onPrimaryContainer = BlackText,
    secondary = BlackText,
    onSecondary = CreamBackground,
    secondaryContainer = CreamSurfaceVariant,
    onSecondaryContainer = BlackText,
    tertiary = PurpleLogo,
    onTertiary = CreamBackground,
    background = CreamBackground,
    onBackground = BlackText,
    surface = CreamSurface,
    onSurface = BlackText,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = BlackText,
    outline = BlackBorder,
    outlineVariant = BlackBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // The user requested: "the whole app will only have 3 colors"
    MaterialTheme(
        colorScheme = StrictColorScheme,
        typography = Typography,
        content = content
    )
}
