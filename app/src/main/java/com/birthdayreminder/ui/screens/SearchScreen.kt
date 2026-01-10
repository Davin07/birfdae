package com.birthdayreminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdayreminder.ui.components.ConfirmationDialog
import com.birthdayreminder.ui.components.LuminaBackground
import com.birthdayreminder.ui.components.LuminaBirthdayCard
import com.birthdayreminder.ui.components.LuminaChip
import com.birthdayreminder.ui.components.LuminaHeader
import com.birthdayreminder.ui.components.LuminaSearchBar
import com.birthdayreminder.ui.viewmodel.SearchType
import com.birthdayreminder.ui.viewmodel.SearchViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToEditBirthday: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    // Need birthdayToDelete state for dialog
    val birthdayToDelete = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Long?>(null) }

    LuminaBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            LuminaHeader(title = "Search")

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
            ) {
                // Header Area Content
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LuminaSearchBar(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChanged,
                        placeholder =
                            if (uiState.searchType == SearchType.NAME) {
                                "Search by name"
                            } else {
                                "Search by month"
                            },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LuminaChip(
                            selected = uiState.searchType == SearchType.NAME,
                            onClick = { viewModel.onSearchTypeChanged(SearchType.NAME) },
                            label = "Name",
                        )
                        LuminaChip(
                            selected = uiState.searchType == SearchType.MONTH,
                            onClick = { viewModel.onSearchTypeChanged(SearchType.MONTH) },
                            label = "Month",
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 140.dp),
                ) {
                    items(uiState.results, key = { it.birthday.id }) { birthday ->
                        val dismissState =
                            rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    when (value) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            birthdayToDelete.value = birthday.birthday.id
                                            false // Snap back, dialog handles delete
                                        }
                                        else -> false
                                    }
                                },
                            )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                if (dismissState.dismissDirection == SwipeToDismissBoxValue.Settled) {
                                    return@SwipeToDismissBox
                                }

                                val color = Color(0xFFD32F2F)
                                val alignment = Alignment.CenterEnd
                                val icon = Icons.Default.Delete

                                val offset =
                                    try {
                                        dismissState.requireOffset()
                                    } catch (e: IllegalStateException) {
                                        0f
                                    }
                                val widthDp = with(LocalDensity.current) { abs(offset).toDp() }

                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent),
                                    contentAlignment = alignment,
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxHeight()
                                                .width(widthDp)
                                                .background(color, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 20.dp),
                                        contentAlignment = alignment,
                                    ) {
                                        Icon(icon, contentDescription = "Delete", tint = Color.White)
                                    }
                                }
                            },
                            content = {
                                LuminaBirthdayCard(
                                    name = birthday.name,
                                    imageUri = birthday.birthday.imageUri,
                                    dateString = birthday.birthDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                                    age = birthday.age,
                                    daysUntil = birthday.daysUntilNext,
                                    isPinned = birthday.birthday.isPinned,
                                    onClick = { onNavigateToEditBirthday(birthday.birthday.id) },
                                )
                            },
                        )
                    }
                }
            }
        }

        // Confirmation Dialog
        if (birthdayToDelete.value != null) {
            ConfirmationDialog(
                title = "Delete Birthday",
                message = "Are you sure you want to delete this birthday?",
                onConfirm = {
                    birthdayToDelete.value?.let { viewModel.deleteBirthday(it) }
                    birthdayToDelete.value = null
                },
                onDismiss = { birthdayToDelete.value = null },
            )
        }
    }
}
