package com.birthdayreminder.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdayreminder.ui.components.NotificationTimePicker
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.components.birfdae.SectionLabel
import com.birthdayreminder.ui.components.birfdae.SettingsGroup
import com.birthdayreminder.ui.components.birfdae.SettingsRow
import com.birthdayreminder.ui.components.birfdae.SettingsToggleRow
import com.birthdayreminder.ui.viewmodel.NotificationSettingsViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Notification and appearance settings.
 *
 * Configure surface: rows that toggle or open a system setting, with the
 * explanation below rather than in a dialog.
 *
 * @param onNavigateBack kept for symmetry with the other bottom-nav destinations;
 *   Settings has nothing to go back to
 * @param onNavigateToPerPerson opens the per-person reminder overrides
 * @param onNavigateToLeadTime opens the same overrides, which is where lead
 *   time is actually edited -- there is no global default to change
 * @param onNavigateToBackup opens the backup screen
 * @param viewModel screen ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPerPerson: () -> Unit,
    onNavigateToLeadTime: () -> Unit,
    onNavigateToBackup: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    SaffronBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeader(title = "Settings")

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SaffronTokens.gutter),
                verticalArrangement = Arrangement.spacedBy(SaffronTokens.space20),
            ) {
                // Flat rows under section labels rather than a stack of cards.
                // These rows are peers -- nothing here outranks anything else --
                // and a card each makes them read as separate destinations
                // instead of one list.

                SettingsGroup(title = "Reminders") {
                    // Notification permission is a system setting, so this row
                    // reports the real state and links out when it needs
                    // changing. A local toggle would look like it worked and
                    // change nothing.
                    SettingsToggleRow(
                        title = "Notifications",
                        subtitle = "Birthday alerts and heads-up",
                        checked = uiState.areNotificationsEnabled,
                        onCheckedChange = { openNotificationSettings(context) },
                    )

                    SettingsRow(
                        title = "Reminder time",
                        subtitle = "When alerts arrive",
                        onClick = { showTimePicker = !showTimePicker },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text =
                                        timeFormatter.format(
                                            LocalTime.of(uiState.defaultHour, uiState.defaultMinute),
                                        ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.width(SaffronTokens.space8))
                                Chevron()
                            }
                        },
                    )

                    // Expanding in place keeps the value next to the control
                    // that changes it; a separate screen would hide the
                    // default while someone decides whether to override it.
                    if (showTimePicker) {
                        Surface(
                            shape = SaffronTokens.radiusCard,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(SaffronTokens.space16)) {
                                NotificationTimePicker(
                                    hour = uiState.defaultHour,
                                    minute = uiState.defaultMinute,
                                    onTimeChange = { h, m -> viewModel.updateDefaultTime(h, m) },
                                )
                            }
                        }
                    }

                    // There is no global lead-time default in the data layer,
                    // only a per-person value, so this opens the same override
                    // screen rather than showing a number stored nowhere.
                    SettingsRow(
                        title = "Lead time",
                        subtitle = "Set per person, 1 to 7 days before",
                        showDivider = false,
                        onClick = onNavigateToLeadTime,
                        trailing = { Chevron() },
                    )
                }

                SettingsGroup(title = "Your data") {
                    SettingsRow(
                        title = "Per-person reminders",
                        subtitle = "Time and lead time, per person",
                        onClick = onNavigateToPerPerson,
                        trailing = { Chevron() },
                    )

                    SettingsRow(
                        title = "Backup & restore",
                        subtitle = "Export or import a .birfdae file",
                        showDivider = false,
                        onClick = onNavigateToBackup,
                        trailing = { Chevron() },
                    )
                }

                SettingsGroup(title = "Appearance") {
                    SettingsToggleRow(
                        title = "Use wallpaper colours",
                        subtitle = "Off \u2014 the brand palette is fixed",
                        checked = uiState.isMaterialYouEnabled,
                        onCheckedChange = { viewModel.toggleMaterialYou(it) },
                        showDivider = false,
                    )
                }

                SectionLabel(title = "How reminders work")

                listOf(
                    "Reminders arrive as a normal notification at the time set for each person.",
                    "The default time is 9:00 AM if a person has none.",
                    "A person can override the time, and lead time, for themselves.",
                    "Nothing opens on its own. Tapping a reminder takes you to that person.",
                ).forEach { point ->
                    Row(
                        modifier = Modifier.padding(bottom = SaffronTokens.space8),
                        horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space8),
                    ) {
                        Text(
                            text = "\u2022",
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(SaffronTokens.navBarHeight))
            }
        }
    }
}

/** The 12-hour clock the reminder time is shown in, matching the picker. */
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

/** Trailing chevron marking a row that opens another screen. */
@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Opens the app's notification settings.
 *
 * Android has no intent that toggles notification permission directly, so the
 * closest thing is the app's notification settings page. The user does the last
 * tap themselves.
 */
private fun openNotificationSettings(context: Context) {
    val intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
    context.startActivity(intent)
}
