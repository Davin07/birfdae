package com.birthdayreminder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LuminaBackground(content: @Composable () -> Unit) {
    val isDark =
        !MaterialTheme.colorScheme.surface.let {
            it.red > 0.5f && it.green > 0.5f && it.blue > 0.5f
        }
    val bgColor = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF2F7F4)

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(bgColor),
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        // Light mode blobs: Green/Teal. Dark mode: subtle.
        val blobAlpha = if (isDark) 0.2f else 0.15f

        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .blur(100.dp),
        ) {
            drawCircle(
                color = primaryColor.copy(alpha = blobAlpha),
                radius = size.width * 0.4f,
                center = Offset(0f, 0f),
            )
            drawCircle(
                color = tertiaryColor.copy(alpha = blobAlpha),
                radius = size.width * 0.5f,
                center = Offset(size.width, size.height),
            )
        }

        content()
    }
}

@Composable
fun LuminaHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp)
                .padding(horizontal = 16.dp),
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            CircleShape,
                        )
                        .align(Alignment.CenterStart),
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Text(
            text = title,
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center),
        )

        if (actions != null) {
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                actions()
            }
        }
    }
}

@Composable
fun LuminaGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isDark =
        !MaterialTheme.colorScheme.surface.let {
            it.red > 0.5f && it.green > 0.5f && it.blue > 0.5f
        }
    val glassColor =
        if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.4f,
            )
        } else {
            Color.White.copy(alpha = 0.7f)
        }
    val shadowElevation = if (isDark) 0.dp else 4.dp
    val shadowColor = if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.1f)

    val borderBrush =
        if (isDark) {
            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.02f)))
        } else {
            // Visible grey border for Light Mode
            Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.1f)))
        }

    Box(
        modifier =
            modifier
                .shadow(shadowElevation, RoundedCornerShape(24.dp), ambientColor = shadowColor, spotColor = shadowColor)
                .clip(RoundedCornerShape(24.dp))
                .background(glassColor)
                .border(width = 1.dp, brush = borderBrush, shape = RoundedCornerShape(24.dp)),
    ) {
        content()
    }
}

@Composable
fun LuminaTitle(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineLarge,
) {
    val color = MaterialTheme.colorScheme.onBackground

    Text(
        text = text,
        style =
            style.copy(
                fontWeight = FontWeight.Bold,
            ),
        color = color,
        modifier = modifier,
    )
}

@Composable
fun LuminaBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                ),
        )
    }
}

@Composable
fun LuminaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val isDark =
        !MaterialTheme.colorScheme.surface.let {
            it.red > 0.5f && it.green > 0.5f && it.blue > 0.5f
        }
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        supportingText = supportingText,
        enabled = enabled,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor =
                    if (isDark) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.3f,
                        )
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    },
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
            ),
    )
}

@Composable
fun LuminaChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val isDark =
        !MaterialTheme.colorScheme.surface.let {
            it.red > 0.5f && it.green > 0.5f && it.blue > 0.5f
        }
    val borderColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else if (isDark) {
            Color.White.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        }

    Surface(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp),
                ),
        color =
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            } else if (isDark) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                Color.White.copy(alpha = 0.7f)
            },
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun LuminaSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val isDark =
        !MaterialTheme.colorScheme.surface.let {
            it.red > 0.5f && it.green > 0.5f && it.blue > 0.5f
        }
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    LuminaGlassCard(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                ),
            singleLine = true,
        )
    }
}

@Composable
fun LuminaAvatar(
    name: String,
    imageUri: String?,
    modifier: Modifier = Modifier,
) {
    val isDark =
        !MaterialTheme.colorScheme.surface.let {
            it.red > 0.5f && it.green > 0.5f && it.blue > 0.5f
        }
    val borderColor =
        if (isDark) {
            Color.White.copy(
                alpha = 0.1f,
            )
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        }

    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        ),
                    ),
                )
                .border(1.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val initials =
                name.trim().split("\\s+".toRegex())
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .joinToString("")
                    .uppercase()
                    .ifEmpty { "?" }

            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun LuminaAvatarPicker(
    imageUri: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark =
        !MaterialTheme.colorScheme.surface.let {
            it.red > 0.5f && it.green > 0.5f && it.blue > 0.5f
        }
    val borderColor =
        if (isDark) {
            Color.White.copy(
                alpha = 0.1f,
            )
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        }
    val bgColor =
        if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f,
            )
        } else {
            Color.White.copy(alpha = 0.6f)
        }

    Box(
        modifier =
            modifier
                .size(144.dp)
                .clickable(onClick = onClick),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Photo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaDatePickerField(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Box {
            LuminaTextField(
                value = selectedDate?.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) ?: "",
                onValueChange = { },
                label = label,
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = isError,
                supportingText = errorMessage?.let { { Text(it) } },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true },
            )
        }
    }

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    selectedDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
                        ?: System.currentTimeMillis(),
                selectableDates =
                    object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            return utcTimeMillis <= System.currentTimeMillis()
                        }
                    },
            )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date =
                                java.time.Instant.ofEpochMilli(
                                    millis,
                                ).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            onDateSelected(date)
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select Birthday",
                        modifier = Modifier.padding(16.dp),
                    )
                },
            )
        }
    }
}

@Composable
fun LuminaBirthdayCard(
    name: String,
    imageUri: String? = null,
    dateString: String,
    age: Int,
    daysUntil: Int,
    isPinned: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val isDark =
        !MaterialTheme.colorScheme.surface.let {
            it.red > 0.5f && it.green > 0.5f && it.blue > 0.5f
        }
    val nameColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    LuminaGlassCard(
        modifier =
            modifier.clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LuminaAvatar(
                name = name,
                imageUri = imageUri,
                modifier = Modifier.size(52.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = nameColor,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$dateString • Turning $age",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            ) {
                Text(
                    text = "$daysUntil Days",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
