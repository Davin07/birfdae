package com.birthdayreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdayreminder.ui.components.LuminaBackground
import com.birthdayreminder.ui.components.LuminaBirthdayCard
import com.birthdayreminder.ui.components.LuminaChip
import com.birthdayreminder.ui.components.LuminaSearchBar
import com.birthdayreminder.ui.components.LuminaTitle
import com.birthdayreminder.ui.viewmodel.SearchType
import com.birthdayreminder.ui.viewmodel.SearchViewModel
import java.time.format.DateTimeFormatter

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
            // Header Area
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LuminaTitle(text = "Search")

                LuminaSearchBar(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChanged,
                    placeholder = if (uiState.searchType == SearchType.NAME) "Search by name" else "Search by month",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LuminaChip(
                        selected = uiState.searchType == SearchType.NAME,
                        onClick = { viewModel.onSearchTypeChanged(SearchType.NAME) },
                        label = "Name"
                    )
                    LuminaChip(
                        selected = uiState.searchType == SearchType.MONTH,
                        onClick = { viewModel.onSearchTypeChanged(SearchType.MONTH) },
                        label = "Month"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(uiState.results) { birthday ->
                    LuminaBirthdayCard(
                        name = birthday.name,
                        dateString = birthday.birthDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                        age = birthday.age,
                        daysUntil = birthday.daysUntilNext,
                        onClick = { onNavigateToEditBirthday(birthday.birthday.id) }
                    )
                }
            }
        }
    }
}