package com.birthdayreminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.birthdayreminder.data.local.entity.Birthday // Added import
import com.birthdayreminder.domain.model.BirthdayWithCountdown
import com.birthdayreminder.ui.theme.BirthdayReminderAppTheme
import java.time.LocalDate
import java.time.Period

/**
 * Reusable card component for displaying birthday information with countdown
 * Satisfies requirements 2.1 (countdown display) and 1.1 (birthday display)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayCard(
    birthday: BirthdayWithCountdown,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with name and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = birthday.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit birthday"
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete birthday",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Birthday date and age with enhanced styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatBirthdayDate(birthday.birthDate),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "Age ${birthday.age}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Countdown display
            CountdownDisplay(
                daysUntilNext = birthday.daysUntilNext,
                isToday = birthday.isToday
            )
            
            // Notes if available
            birthday.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Component for displaying countdown information
 * Satisfies requirement 2.1 (countdown display)
 */
@Composable
private fun CountdownDisplay(
    daysUntilNext: Int,
    isToday: Boolean
) {
    val text = when {
        isToday -> "🎉 Today!"
        daysUntilNext == 1 -> "🎂 Tomorrow!"
        daysUntilNext <= 7 -> "⏰ $daysUntilNext days left"
        else -> "📅 $daysUntilNext days left"
    }
    
    val containerColor = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        daysUntilNext == 1 -> MaterialTheme.colorScheme.secondaryContainer
        daysUntilNext <= 7 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        daysUntilNext == 1 -> MaterialTheme.colorScheme.onSecondaryContainer
        daysUntilNext <= 7 -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            
            if (isToday) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Helper function to format birthday date for display
 * Marked as stable for performance optimization
 */
@Stable
private fun formatBirthdayDate(date: LocalDate): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$month ${date.dayOfMonth}"
}

/**
 * Simplified birthday card for calendar view with click action
 * Used in calendar screen when showing birthdays for a selected date
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayCard(
    birthday: BirthdayWithCountdown,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Name
            Text(
                text = birthday.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Birthday date and age
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatBirthdayDate(birthday.birthDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Age ${birthday.age}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Countdown display (compact version)
            if (birthday.isToday) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🎉 Today!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BirthdayCardPreview() {
    BirthdayReminderAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's birthday
            val todayBirthDate = LocalDate.now() // For "Today"
            val person1Age = 30 // Example age
            val birthday1 = Birthday(
                id = 1,
                name = "John Doe",
                birthDate = todayBirthDate.minusYears(person1Age.toLong()), // Actual birth date in the past
                notes = "Best friend from college"
            )
            BirthdayCard(
                birthday = BirthdayWithCountdown(
                    birthday = birthday1,
                    daysUntilNext = 0,
                    isToday = true,
                    nextOccurrence = todayBirthDate,
                    age = person1Age
                ),
                onEditClick = {},
                onDeleteClick = {}
            )
            
            // Upcoming birthday
            val upcomingBirthDate = LocalDate.now().plusDays(5) // Next occurrence is in 5 days
            val person2Age = 25 // Example age
            val birthday2 = Birthday(
                id = 2,
                name = "Jane Smith",
                birthDate = upcomingBirthDate.minusYears(person2Age.toLong()), // Actual birth date in the past
                notes = null
            )
            BirthdayCard(
                birthday = BirthdayWithCountdown(
                    birthday = birthday2,
                    daysUntilNext = 5,
                    isToday = false,
                    nextOccurrence = upcomingBirthDate,
                    age = person2Age
                ),
                onEditClick = {},
                onDeleteClick = {}
            )
        }
    }
}
