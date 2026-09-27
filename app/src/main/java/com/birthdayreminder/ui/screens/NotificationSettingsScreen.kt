package com.birthdayreminder.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.birthdayreminder.ui.components.NotificationTimePicker
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.components.birfdae.SectionLabel
import com.birthdayreminder.ui.navigation.BirthdayNavigation
import com.birthdayreminder.ui.viewmodel.NotificationSettingsViewModel

/**
 * Notification and appearance settings.
 *
 * Configure surface: rows that toggle or open a system setting, with the
 * explanation below rather than in a dialog.
 *
 * @param onNavigateBack unused; the screen is a bottom-nav destination
 * @param navController used to reach the backup screen
 * @param viewModel screen ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    SaffronBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeader(title = "Settings")

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SaffronTokens.gutter),
                verticalArrangement = Arrangement.spacedBy(SaffronTokens.space12),
            ) {
                Surface(
                    shape = SaffronTokens.radiusCard,
                    color =
                        if (uiState.areNotificationsEnabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                    contentColor =
                        if (uiState.areNotificationsEnabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(SaffronTokens.space20),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        shape = CircleShape,
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector =
                                    if (uiState.areNotificationsEnabled) {
                                        Icons.Default.Notifications
                                    } else {
                                        Icons.Outlined.Notifications
                                    },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Spacer(Modifier.width(SaffronTokens.space16))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text =
                                    if (uiState.areNotificationsEnabled) {
                                        "Notifications are on"
                                    } else {
                                        "Notifications are off"
                                    },
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (!uiState.areNotificationsEnabled) {
                                TextButton(
                                    onClick = {
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
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                ) {
                                    Text("Turn on in system settings")
                                }
                            }
                        }
                    }
                }

                Surface(
                    shape = SaffronTokens.radiusCard,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(modifier = Modifier.padding(SaffronTokens.space20)) {
                        Text(
                            text = "Default reminder time",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(SaffronTokens.space12))
                        NotificationTimePicker(
                            hour = uiState.defaultHour,
                            minute = uiState.defaultMinute,
                            onTimeChange = { h, m -> viewModel.updateDefaultTime(h, m) },
                        )
                    }
                }

                Surface(
                    shape = SaffronTokens.radiusCard,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(SaffronTokens.space20),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use wallpaper colours",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Off keeps the Saffron palette. On follows your system theme.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = uiState.isMaterialYouEnabled,
                            onCheckedChange = { viewModel.toggleMaterialYou(it) },
                        )
                    }
                }

                Surface(
                    onClick = { navController.navigate(BirthdayNavigation.BACKUP) },
                    shape = SaffronTokens.radiusCard,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(SaffronTokens.space20),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Spacer(Modifier.width(SaffronTokens.space16))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Backup & restore",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Export or import a .birfdae file",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                SectionLabel(title = "How reminders work")

                listOf(
                    "Reminders arrive as a normal notification at the time set for each person.",
                    "The default time is 9:00 AM if a person has none.",
                    "Advance reminders can be set for 1, 3 or 7 days before.",
                    "Nothing opens on its own. Tapping a reminder takes you to that person.",
                ).forEach { point ->
                    Row(modifier = Modifier.padding(bottom = SaffronTokens.space8)) {
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = SaffronTokens.space8),
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
