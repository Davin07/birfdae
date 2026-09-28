package com.birthdayreminder.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/*
 * Birf Dae Saffron type scale.
 *
 * These are **not** the stock Material 3 sizes. The approved prototype defines
 * its own scale with steps Material has no name for -- 9.5, 10.5, 12.5 and
 * 14.5px -- and building the app on M3's defaults made it a near neighbour of
 * the design rather than the design itself: every sub-11px style was rounded up
 * to 11sp, and several mid sizes drifted up with it. The hero headline landed
 * at 24sp against a specified 22, and the countdown at 22sp against 17.
 *
 * The prototype renders 1:1 between px and dp -- its avatar is 42px against the
 * app's 42dp avatar, its nav bar 72px against 72dp -- so a prototype px value is
 * the intended sp value, reproduced here as a 1:1 pair.
 *
 * One deliberate deviation, MIN_READABLE: the prototype's 9.5px and 10.5px
 * steps are lifted to 11sp, because text that small stops being legible for
 * older users and does not survive a large font-scale setting. Everything at
 * 12.5px and above is verbatim. Token names are unchanged, so no call site
 * needs editing; only the numbers moved.
 */

// The floor applied to any prototype step below 11px.
//
// Lower this to 0.0 to restore the prototype's 9.5px and 10.5px exactly, if
// the visual match is later judged to be worth the legibility cost.
private const val MIN_READABLE = 11.0

val Typography =
    Typography(
        // --- Display: Fraunces, the voice of the artifact --------------------
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
        // The hero headline. Prototype .h2: 22px, weight 600, lh 1.14. Material's
        // 30sp default is nearly 40% larger and was the single most visible type
        // divergence on the home screen.
        displaySmall =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 25.sp,
                letterSpacing = (-0.01).em,
            ),
        // Prototype .ttl, a screen title: 20px / 600.
        headlineLarge =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                letterSpacing = (-0.02).em,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                letterSpacing = (-0.02).em,
            ),
        // Prototype .who is the name on the card artifact at 38px, but M3's
        // headlineSmall is also used by dialog and sheet titles, where 38sp
        // would be absurd. The card has its own style, [CardArtifactName].
        headlineSmall =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                letterSpacing = (-0.02).em,
            ),
        // Prototype .dy, the countdown: 17px / 800 / Fraunces.
        titleLarge =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.0.em,
            ),
        // Prototype .h3, a section heading: 16px / 600 / Fraunces.
        titleMedium =
            TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.0.em,
            ),
        // Prototype .nm, a person's name: 14px / 800.
        titleSmall =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                letterSpacing = 0.01.em,
            ),
        // --- Body: Figtree ---------------------------------------------------
        // Prototype .inp, a text field: 14px / 600.
        bodyLarge =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.01.em,
            ),
        // Prototype .body, running copy: 12.5px / 400 / lh 1.5.
        bodyMedium =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                lineHeight = 19.sp,
                letterSpacing = 0.01.em,
            ),
        // Prototype .tiny / .sub: 10.5px, lifted to MIN_READABLE.
        bodySmall =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Normal,
                fontSize = maxOf(10.5, MIN_READABLE).sp,
                lineHeight = 15.sp,
                letterSpacing = 0.02.em,
            ),
        // Prototype .btn, a button label: 14px / 800.
        labelLarge =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.01.em,
            ),
        // Prototype .chip: 12.5px / 700.
        labelMedium =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.02.em,
            ),
        // Prototype .eyebrow: 9.5px / 800, lifted to MIN_READABLE.
        labelSmall =
            TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = maxOf(9.5, MIN_READABLE).sp,
                lineHeight = 15.sp,
                letterSpacing = 0.03.em,
            ),
    )

// --- Card artifact ---------------------------------------------------------
//
// The card is the one surface the user actually exports, and it is rendered to
// a bitmap as well as shown on screen. Its type is therefore specified apart
// from the M3 slots, which are shared with dialogs and screens where the same
// step means something much smaller.

/**
 * The name on the card. Prototype `.who`: 38px, Fraunces, weight 600.
 *
 * Not an M3 slot on purpose -- see [Typography]'s `headlineSmall`.
 */
val CardArtifactName =
    TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 38.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.02).em,
    )

/**
 * The message on the card. Prototype `.quote`: 15px, Fraunces, lh 1.4.
 */
val CardArtifactQuote =
    TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.0.em,
    )

/**
 * The attribution on the card. Prototype `.sig`: 11px, weight 800.
 */
val CardArtifactSignature =
    TextStyle(
        fontFamily = Figtree,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.03.em,
    )
