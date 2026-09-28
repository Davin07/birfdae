package com.birthdayreminder

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Swaps [BirthdayReminderApplication] for [HiltTestApplication] in instrumented
 * tests.
 *
 * Without this, every `@HiltAndroidTest` fails at startup with "cannot use a
 * @HiltAndroidApp application".
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
