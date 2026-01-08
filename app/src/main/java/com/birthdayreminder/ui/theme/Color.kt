package com.birthdayreminder.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Lumina Theme Color Palette
 * Derived from reference designs.
 */

// Primary Colors
val LuminaPrimary = Color(0xFF80D0C7)
val LuminaPrimaryDark = Color(0xFF004D40)
val LuminaAccent = Color(0xFFE0A9AF)

// Dark Theme Colors
val LuminaBackgroundDark = Color(0xFF0F1110)
val LuminaSurfaceDark = Color(0xFF1A1D1C)
val LuminaOnBackgroundDark = Color(0xFFE2E2E2)
val LuminaOnSurfaceDark = Color(0xFFE2E2E2)
val LuminaCardGlass = Color(0xA61E2321) // rgba(30, 35, 33, 0.65)

// Light Theme Colors (Inferred)
val LuminaBackgroundLight = Color(0xFFF5F7F6)
val LuminaSurfaceLight = Color(0xFFFFFFFF)
val LuminaOnBackgroundLight = Color(0xFF1A1D1C)
val LuminaOnSurfaceLight = Color(0xFF1A1D1C)

// Gradients
val LuminaNameGradient = listOf(Color(0xFFFFFFFF), Color(0xFFB0BEC5))
val LuminaPrimaryGradient = listOf(Color(0xFF80D0C7), Color(0xFF4DB6AC))
val LuminaCountdownGradient = listOf(Color(0x2680D0C7), Color(0x4D004D40)) // 15% and 30% opacity