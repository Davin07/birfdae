package com.birthdayreminder.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Birf Dae Saffron UI Material 3 Typography Scale.
 *
 * Fraunces: Display & Headline styles.
 * Figtree: Title, Body & Label styles.
 */
val Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = (-0.02).em,
            ),
        displayMedium =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = (-0.03).em,
            ),
        displaySmall =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.02).em,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.02).em,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.01).em,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                letterSpacing = (-0.02).em,
            ),
        titleLarge =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.0.em,
            ),
        titleMedium =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.01.em,
            ),
        titleSmall =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.01.em,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.01.em,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.01.em,
            ),
        bodySmall =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.0.em,
            ),
        labelLarge =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.01.em,
            ),
        labelMedium =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.01.em,
            ),
        labelSmall =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.03.em,
            ),
    )
