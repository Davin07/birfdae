package com.birthdayreminder

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * A Hilt-enabled host for Compose UI tests.
 *
 * `hilt-android-testing` does not ship a ready-made one, and `createComposeRule`
 * hosts a bare ComponentActivity with no Hilt component, so it cannot supply
 * the ViewModels the real screens inject.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
