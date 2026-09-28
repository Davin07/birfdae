package com.birthdayreminder.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Material 3 Saffron Design System Color Palette.
 * Derived from primary seed #C98A12 using Google's HCT algorithm.
 * Error colors are decoupled from the saffron hue and locked to M3 red tokens.
 */

// --- Core HCT Palette Seeds ---
val SaffronSeed = Color(0xFFC98A12)
val RosewoodSeed = Color(0xFF8F4B3D)
val PlumSeed = Color(0xFF844788)

// ==========================================
// Light Theme Saffron Tokens
// ==========================================
val SaffronLightPrimary = Color(0xFF815600)
val SaffronLightOnPrimary = Color(0xFFFFFFFF)
val SaffronLightPrimaryContainer = Color(0xFFFFDDB1)
val SaffronLightOnPrimaryContainer = Color(0xFF291800)
val SaffronLightInversePrimary = Color(0xFFFFBA49)

val SaffronLightSecondary = Color(0xFF8F4B3D)
val SaffronLightOnSecondary = Color(0xFFFFFFFF)
val SaffronLightSecondaryContainer = Color(0xFFFFDAD3)
val SaffronLightOnSecondaryContainer = Color(0xFF3A0A02)

val SaffronLightTertiary = Color(0xFF844788)
val SaffronLightOnTertiary = Color(0xFFFFFFFF)
val SaffronLightTertiaryContainer = Color(0xFFFFD6FB)
val SaffronLightOnTertiaryContainer = Color(0xFF36003D)

val SaffronLightError = Color(0xFFBA1A1A)
val SaffronLightOnError = Color(0xFFFFFFFF)
val SaffronLightErrorContainer = Color(0xFFFFDAD6)
val SaffronLightOnErrorContainer = Color(0xFF410002)

val SaffronLightBackground = Color(0xFFFFF8F3)
val SaffronLightOnBackground = Color(0xFF291800)
val SaffronLightSurface = Color(0xFFFFF8F3)
val SaffronLightOnSurface = Color(0xFF291800)
val SaffronLightSurfaceVariant = Color(0xFFFFDDB1)
val SaffronLightOnSurfaceVariant = Color(0xFF614000)
val SaffronLightSurfaceTint = Color(0xFF815600)

val SaffronLightSurfaceBright = Color(0xFFFFF8F3)
val SaffronLightSurfaceDim = Color(0xFFFFD395)
val SaffronLightSurfaceContainerLowest = Color(0xFFFFFFFF)
val SaffronLightSurfaceContainerLow = Color(0xFFFFF1E3)
val SaffronLightSurfaceContainer = Color(0xFFFFEBD3)
val SaffronLightSurfaceContainerHigh = Color(0xFFFFE4C2)
val SaffronLightSurfaceContainerHighest = Color(0xFFFFDDB1)

val SaffronLightOutline = Color(0xFFA16C00)
val SaffronLightOutlineVariant = Color(0xFFE9C08A)
val SaffronLightInverseSurface = Color(0xFF442B00)
val SaffronLightInverseOnSurface = Color(0xFFFFEEDB)
val SaffronLightScrim = Color(0xFF000000)

// ==========================================
// Dark Theme Saffron Tokens
// ==========================================
val SaffronDarkPrimary = Color(0xFFFFBA49)
val SaffronDarkOnPrimary = Color(0xFF442B00)
val SaffronDarkPrimaryContainer = Color(0xFF614000)
val SaffronDarkOnPrimaryContainer = Color(0xFFFFDDB1)
val SaffronDarkInversePrimary = Color(0xFF815600)

val SaffronDarkSecondary = Color(0xFFFFB4A5)
val SaffronDarkOnSecondary = Color(0xFF3A0A02)
val SaffronDarkSecondaryContainer = Color(0xFF6C2D22)
val SaffronDarkOnSecondaryContainer = Color(0xFFFFDAD3)

val SaffronDarkTertiary = Color(0xFFF7AEF7)
val SaffronDarkOnTertiary = Color(0xFF36003D)
val SaffronDarkTertiaryContainer = Color(0xFF6A2F6E)
val SaffronDarkOnTertiaryContainer = Color(0xFFFFD6FB)

val SaffronDarkError = Color(0xFFFFB4AB)
val SaffronDarkOnError = Color(0xFF690005)
val SaffronDarkErrorContainer = Color(0xFF93000A)
val SaffronDarkOnErrorContainer = Color(0xFFFFDAD6)

val SaffronDarkBackground = Color(0xFF1E1100)
val SaffronDarkOnBackground = Color(0xFFFFDDB1)
val SaffronDarkSurface = Color(0xFF1E1100)
val SaffronDarkOnSurface = Color(0xFFFFDDB1)
val SaffronDarkSurfaceVariant = Color(0xFF614000)
val SaffronDarkOnSurfaceVariant = Color(0xFFFFBA49)
val SaffronDarkSurfaceTint = Color(0xFFFFBA49)

val SaffronDarkSurfaceBright = Color(0xFF503400)
val SaffronDarkSurfaceDim = Color(0xFF1E1100)
val SaffronDarkSurfaceContainerLowest = Color(0xFF170C00)
val SaffronDarkSurfaceContainerLow = Color(0xFF291800)
val SaffronDarkSurfaceContainer = Color(0xFF2E1C00)
val SaffronDarkSurfaceContainerHigh = Color(0xFF3C2600)
val SaffronDarkSurfaceContainerHighest = Color(0xFF4A2F00)

val SaffronDarkOutline = Color(0xFFC28408)
val SaffronDarkOutlineVariant = Color(0xFF614000)
val SaffronDarkInverseSurface = Color(0xFFFFDDB1)
val SaffronDarkInverseOnSurface = Color(0xFF442B00)
val SaffronDarkScrim = Color(0xFF000000)

// --- Overdue -----------------------------------------------------------------
// The plan calls overdue "warm-urgent, not alarming red" (§5.4), so it sits in
// the rosewood family rather than reusing tertiary, which the tonal palette
// generated as plum. A missed birthday should read as something to act on, not
// as an error state and not as a different product.
val SaffronLightOverdueContainer = Color(0xFFFFE0B8)
val SaffronLightOnOverdueContainer = Color(0xFF2A1700)
val SaffronDarkOverdueContainer = Color(0xFF5C3A00)
val SaffronDarkOnOverdueContainer = Color(0xFFFFE0B8)
