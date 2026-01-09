package com.birthdayreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdayreminder.ui.components.BirthdayCard
import com.birthdayreminder.ui.components.LuminaBackground
import com.birthdayreminder.ui.components.LuminaTextField
import com.birthdayreminder.ui.components.LuminaTitle
import com.birthdayreminder.ui.viewmodel.SearchType
import com.birthdayreminder.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToEditBirthday: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LuminaBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            LuminaTitle(text = "Search")
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.searchType == SearchType.NAME,
                    onClick = { viewModel.onSearchTypeChanged(SearchType.NAME) },
                    label = { Text("Name") }
                )
                FilterChip(
                    selected = uiState.searchType == SearchType.MONTH,
                    onClick = { viewModel.onSearchTypeChanged(SearchType.MONTH) },
                    label = { Text("Month") }
                )
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.results) { birthday ->
                    BirthdayCard(
                        birthday = birthday,
                        onClick = { onNavigateToEditBirthday(birthday.birthday.id) }
                    )
                }
            }
            
            // Search Bar at Bottom (Padded to clear Floating Nav Bar)
            LuminaTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChanged,
                label = if (uiState.searchType == SearchType.NAME) "Search by name" else "Search by month (e.g. January)",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 96.dp)
            )
        }
    }
}