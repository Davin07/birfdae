package com.birthdayreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthdayreminder.ui.components.birfdae.LeadTimeChip
import com.birthdayreminder.ui.components.birfdae.SaffronBackground
import com.birthdayreminder.ui.components.birfdae.SaffronTokens
import com.birthdayreminder.ui.components.birfdae.SectionHeader
import com.birthdayreminder.ui.components.birfdae.SectionLabel
import com.birthdayreminder.ui.components.birfdae.SettingsToggleRow
import com.birthdayreminder.ui.viewmodel.LEAD_TIME_CHOICES
import com.birthdayreminder.ui.viewmodel.PerPersonReminderViewModel
import com.birthdayreminder.ui.viewmodel.leadTimeLabel
import java.time.format.DateTimeFormatter

/**
 * Per-person reminder overrides.
 *
 * Reached from Settings, which is where someone goes to answer "why did I get
 * no reminder for this person?". Each row owns its own switch and its own
 * lead time, so nothing here changes a value for everybody.
 *
 * @param onNavigateBack returns to Settings
 * @param viewModel screen ViewModel
 */
@Composable
fun PerPersonReminderScreen(
    onNavigateBack: () -> Unit,
    viewModel: PerPersonReminderViewModel = hiltViewModel(),
) {
    val people by viewModel.people.collectAsStateWithLifecycle()
    val errors by viewModel.errors.collectAsStateWithLifecycle()

    SaffronBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeader(title = "Per-person reminders", onBackClick = onNavigateBack)

            if (people.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(SaffronTokens.gutter),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Nobody to remind yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(SaffronTokens.space8))
                    Text(
                        text = "Add someone and their reminder settings will show up here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        androidx.compose.foundation.layout.PaddingValues(
                            start = SaffronTokens.gutter,
                            end = SaffronTokens.gutter,
                            bottom = SaffronTokens.navBarHeight,
                        ),
                ) {
                    item {
                        SectionLabel(title = "These override the default time and lead time")
                    }

                    items(people, key = { it.id }) { person ->
                        Column {
                            SettingsToggleRow(
                                title = person.name,
                                subtitle = person.birthDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                                checked = person.notificationsEnabled,
                                onCheckedChange = { viewModel.setNotificationsEnabled(person.id, it) },
                                showDivider = false,
                            )

                            // The lead-time chips sit under their own person's
                            // row rather than in a separate screen, so the
                            // value and the person it belongs to cannot drift
                            // apart while scrolling.
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = SaffronTokens.space12),
                                horizontalArrangement = Arrangement.spacedBy(SaffronTokens.space8),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                LEAD_TIME_CHOICES.forEach { days ->
                                    LeadTimeChip(
                                        label = leadTimeLabel(days),
                                        selected = person.leadTimeDays == days,
                                        onClick = { viewModel.setLeadTime(person.id, days) },
                                    )
                                }
                            }

                            errors[person.id]?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = SaffronTokens.space8),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
