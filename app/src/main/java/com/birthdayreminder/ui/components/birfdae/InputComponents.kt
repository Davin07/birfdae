package com.birthdayreminder.ui.components.birfdae

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Themed text field on a flat container fill.
 *
 * Replaces the old text field, which guessed dark mode by inspecting
 * `colorScheme.surface` and then hard-coded `Color.White` for light mode. All
 * colour now comes from the colour scheme, so light and dark are handled by
 * the theme instead of by branches inside the component.
 *
 * @param value current text
 * @param onValueChange invoked on each edit
 * @param label field label
 * @param modifier applied to the field
 * @param placeholder optional hint shown when empty
 * @param singleLine whether the field is single-line
 * @param minLines minimum visible lines
 * @param isError whether to show the error treatment
 * @param errorMessage error text shown below the field
 * @param enabled whether the field accepts input
 * @param readOnly whether the field is display-only
 * @param leadingIcon optional leading icon
 * @param trailingIcon optional trailing icon
 */
@Composable
fun SaffronTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            shape = SaffronTokens.radiusMedium,
            textStyle = MaterialTheme.typography.bodyLarge,
            colors =
                saffronFieldColors(
                    focusedContainer = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainer = MaterialTheme.colorScheme.surfaceContainer,
                ),
        )
        SaffronFieldError(isError = isError, errorMessage = errorMessage)
    }
}

/**
 * Rounded search field.
 *
 * @param value current query
 * @param onValueChange invoked on each edit
 * @param placeholder hint text
 * @param modifier applied to the field
 */
@Composable
fun SaffronSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = SaffronTokens.minFieldHeight),
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        shape = CircleShape,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors =
            saffronFieldColors(
                focusedContainer = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainer = MaterialTheme.colorScheme.surfaceContainer,
                hideIndicator = true,
            ),
    )
}

/**
 * Read-only field that opens the Material 3 date picker.
 *
 * Future dates are not selectable: a birthday cannot be in the future.
 *
 * @param selectedDate currently chosen date, or null
 * @param onDateSelected invoked with the chosen date
 * @param label field label
 * @param modifier applied to the field
 * @param isError whether to show the error treatment
 * @param errorMessage error text shown below the field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaffronDateField(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy") }

    Column(modifier = modifier.fillMaxWidth()) {
        Box {
            TextField(
                value = selectedDate?.format(formatter) ?: "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = isError,
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.DateRange,
                        contentDescription = "Select date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                shape = SaffronTokens.radiusMedium,
                textStyle = MaterialTheme.typography.bodyLarge,
                colors =
                    saffronFieldColors(
                        focusedContainer = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainer = MaterialTheme.colorScheme.surfaceContainer,
                    ),
            )
            // The field is display-only, so the whole surface is the tap target.
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clickable { showPicker = true },
            )
        }
        SaffronFieldError(isError = isError, errorMessage = errorMessage)
    }

    if (showPicker) {
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    selectedDate?.toEpochDay()?.times(86_400_000L)
                        ?: System.currentTimeMillis(),
                selectableDates =
                    object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                            utcTimeMillis <= System.currentTimeMillis()
                    },
            )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(
                                Instant
                                    .ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate(),
                            )
                        }
                        showPicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/**
 * Tappable photo slot.
 *
 * @param imageUri current image, or null when none is set
 * @param onClick invoked on tap
 * @param modifier applied to the slot
 * @param size slot diameter
 */
@Composable
fun SaffronAvatarPicker(
    imageUri: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = SaffronTokens.avatarLarge,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add photo",
                    modifier = Modifier.size(size / 2),
                )
            }
        }
    }
}

/** Inline error text shown under a field. */
@Composable
private fun SaffronFieldError(
    isError: Boolean,
    errorMessage: String?,
) {
    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = SaffronTokens.space16, top = SaffronTokens.space4),
        )
    }
}

/** Shared text-field colours so every input in the app matches. */
@Composable
private fun saffronFieldColors(
    focusedContainer: Color,
    unfocusedContainer: Color,
    hideIndicator: Boolean = false,
) = TextFieldDefaults.colors(
    focusedContainerColor = focusedContainer,
    unfocusedContainerColor = unfocusedContainer,
    disabledContainerColor = unfocusedContainer,
    errorContainerColor = unfocusedContainer,
    focusedIndicatorColor = if (hideIndicator) Color.Transparent else MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = if (hideIndicator) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = MaterialTheme.colorScheme.error,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorLabelColor = MaterialTheme.colorScheme.error,
)
