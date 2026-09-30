package com.birthdayreminder

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.birthdayreminder.data.settings.SettingsRepository
import com.birthdayreminder.ui.BirthdayApp
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepository: SettingsRepository

    @Inject lateinit var reminderRecorder: NotificationTapRecorder

    /**
     * The person whose notification was tapped, or null.
     *
     * Held as state rather than read once in onCreate because the common case
     * is a tap while the app is already running, which arrives at onNewIntent.
     */
    private var notificationBirthdayId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate. On Android 12+ the platform
        // splash is already showing; this is the pre-12 fallback.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        consumeNotificationIntent(intent)

        setContent {
            // Follow the system light/dark setting. This used to be hard-coded
            // to true, which made the app dark-only.
            val useDarkTheme = isSystemInDarkTheme()

            BirthdayReminderAppTheme(darkTheme = useDarkTheme) {
                RequestNotificationPermission()

                BirthdayApp(
                    notificationBirthdayId = notificationBirthdayId,
                    onNotificationHandled = { notificationBirthdayId = null },
                )
            }
        }
    }

    /**
     * A tap on a birthday notification while the app is already open.
     *
     * Without this the tap would do nothing: the intent carries the id but a
     * running activity is not re-created, so onCreate never sees the second
     * tap. This is also the event that feeds the acknowledgement streak, so
     * missing it would silently under-count.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeNotificationIntent(intent)
    }

    /**
     * Records the acknowledgement and navigates to that person's card.
     *
     * The recording and the navigation are the same tap, which is the point:
     * the streak counts something the user did, at the moment they did it.
     *
     * @param intent the intent that may carry a notification payload
     */
    private fun consumeNotificationIntent(intent: Intent?) {
        val birthdayId = intent?.getLongExtra(EXTRA_BIRTHDAY_ID, NO_BIRTHDAY_ID) ?: return
        if (birthdayId == NO_BIRTHDAY_ID) return

        notificationBirthdayId = birthdayId

        // The year is the birthday's, not today's: a reminder that fires three
        // days early still belongs to the year the birthday falls in, and
        // recording it under the current year would credit the wrong year.
        val year = intent.getIntExtra(EXTRA_BIRTHDAY_YEAR, LocalDateTime.now().year)
        lifecycleScope.launch {
            reminderRecorder.recordAcknowledged(birthdayId, year)
        }
    }

    companion object {
        /** Extra carrying the person a notification is about. */
        const val EXTRA_BIRTHDAY_ID = "birthday_id"

        /**
         * Extra carrying the year that birthday falls in.
         *
         * Set by the scheduler so an advance reminder is credited to the right
         * year rather than the one the notification happened to fire in.
         */
        const val EXTRA_BIRTHDAY_YEAR = "birthday_year"

        /** Sentinel for "this intent is not a notification tap". */
        const val NO_BIRTHDAY_ID = -1L
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    // Handle result if needed
                },
            )

        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait",
)
@Composable
fun birthdayAppPreview() {
    BirthdayReminderAppTheme {
        BirthdayApp()
    }
}
