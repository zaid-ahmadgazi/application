package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Strict 3-Color Design System as requested:
// 1. Purple for logo
// 2. Cream color for background
// 3. Black for border and text
// =========================================================================

val PurpleLogo = Color(0xFF7E22CE)         // Purple for logo (vivid, royal purple)
val PurpleLogoDark = Color(0xFF6B21A8)     // Deep purple variant
val PurpleLogoLight = Color(0xFF9333EA)    // Bright purple variant

val CreamBackground = Color(0xFFFAF7F0)    // Warm soft cream background
val CreamSurface = Color(0xFFFFFDF8)       // Crisp soft cream for cards / surface
val CreamSurfaceVariant = Color(0xFFF3EFE6)// Slightly deeper cream for chips / highlights

val BlackText = Color(0xFF000000)          // Jet black for text
val BlackBorder = Color(0xFF000000)        // Pure black for all borders
val BlackMuted = Color(0xFF222222)         // Deep black for subtitles/secondary text

// Compatibility aliases so all existing references resolve cleanly to the 3-color system
val RoseCream = CreamBackground
val RoseSurface = CreamSurface
val RoseSurfaceVariant = CreamSurfaceVariant

val PlumHeader = BlackText
val PlumPrimary = BlackText
val PlumDark = BlackText
val PlumLight = BlackText

val RoseGoldAccent = BlackText
val RoseGoldSoft = CreamSurfaceVariant
val RoseGoldBorder = BlackBorder

val RoseAccent = BlackText
val RoseSoft = CreamSurfaceVariant
val RoseDeep = BlackText

val GoldAccent = BlackText
val GoldSoft = CreamSurfaceVariant

val GreenSuccess = BlackText
val GreenSoft = CreamSurfaceVariant

// Font colors: pure black for maximum contrast and legibility
val TextPrimary = BlackText
val TextSecondary = BlackText
val TextMuted = BlackMuted

// Dark Theme Colors mapped to Cream & Black to strictly honor user request
val DarkBackground = CreamBackground
val DarkSurface = CreamSurface
val DarkSurfaceVariant = CreamSurfaceVariant
val DarkPlumPrimary = BlackText
val DarkRoseAccent = BlackText
val DarkTextPrimary = BlackText
val DarkTextSecondary = BlackText
val DarkTextMuted = BlackMuted
