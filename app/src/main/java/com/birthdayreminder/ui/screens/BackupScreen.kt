package com.birthdayreminder.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdayreminder.R
import com.birthdayreminder.data.backup.ConflictStrategy
import com.birthdayreminder.ui.viewmodel.BackupViewModel
import com.birthdayreminder.ui.viewmodel.BackupUiState

/**
 * Screen for backup and restore operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showConflictDialog by remember { mutableStateOf(false) }
    
    // Activity result launchers for file operations
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.exportBirthdays(it) }
        }
    )
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { 
                // Store the URI and validate the file first
                pendingImportUri = it
                viewModel.validateBackupFile(it)
            }
        }
    )
    
    // Clear success messages after a delay
    LaunchedEffect(uiState.exportSuccess) {
        if (uiState.exportSuccess) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearExportSuccess()
        }
    }
    
    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearImportSuccess()
        }
    }
    
    LaunchedEffect(uiState.validationSuccess) {
        if (uiState.validationSuccess) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearValidationSuccess()
        }
    }
    
    // When validation is successful and we have a pending import, show the conflict dialog
    LaunchedEffect(uiState.validationSuccess, uiState.isFileValid) {
        if (uiState.validationSuccess && uiState.isFileValid == true && pendingImportUri != null) {
            showConflictDialog = true
        } else if (uiState.validationSuccess && uiState.isFileValid == false) {
            // File is invalid, clear the pending URI
            pendingImportUri = null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Export section
            BackupExportSection(
                viewModel = viewModel,
                uiState = uiState,
                onExportClick = { fileName ->
                    exportLauncher.launch(fileName)
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Import section
            BackupImportSection(
                viewModel = viewModel,
                uiState = uiState,
                onImportClick = {
                    importLauncher.launch(arrayOf("application/json"))
                }
            )
            
            // Error messages
            if (uiState.exportError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorMessage(message = uiState.exportError!!)
            }
            
            if (uiState.importError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorMessage(message = uiState.importError!!)
            }
            
            if (uiState.validationError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorMessage(message = uiState.validationError!!)
            }
            
            // File validation result message
            if (uiState.validationSuccess && uiState.isFileValid == false) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Invalid File",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "The selected file is not a valid backup file.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
    
    // Conflict strategy selection dialog
    if (showConflictDialog && pendingImportUri != null) {
        ConflictStrategyDialog(
            onDismiss = {
                showConflictDialog = false
                pendingImportUri = null
            },
            onStrategySelected = { strategy ->
                pendingImportUri?.let { uri ->
                    viewModel.importBirthdays(uri, strategy)
                }
                showConflictDialog = false
                pendingImportUri = null
            }
        )
    }
}

/**
 * Section for exporting birthdays.
 */
@Composable
private fun BackupExportSection(
    viewModel: BackupViewModel,
    uiState: BackupUiState,
    onExportClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SaveAlt,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Birthdays",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Create a backup of all your birthdays to a JSON file. You can save this file to your device or cloud storage.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val fileName = viewModel.generateDefaultBackupFileName()
                    onExportClick(fileName)
                },
                enabled = !uiState.isExporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Export to File")
            }
            
            if (uiState.exportSuccess) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Backup created successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Section for importing birthdays.
 */
@Composable
private fun BackupImportSection(
    viewModel: BackupViewModel,
    uiState: BackupUiState,
    onImportClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Restore Birthdays",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Restore your birthdays from a previously created backup file. Select a JSON backup file to import.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onImportClick,
                enabled = !uiState.isImporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Import from File")
            }
            
            if (uiState.importSuccess) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Backup restored successfully! ${uiState.importedCount ?: 0} birthdays imported.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Displays an error message.
 */
@Composable
private fun ErrorMessage(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * Dialog for selecting a conflict strategy when importing birthdays.
 */
@Composable
private fun ConflictStrategyDialog(
    onDismiss: () -> Unit,
    onStrategySelected: (ConflictStrategy) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import Conflicts",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text("Some birthdays in the backup file already exist. How would you like to handle these conflicts?")
        },
        confirmButton = {
            Column {
                // Skip option
                TextButton(
                    onClick = {
                        onStrategySelected(ConflictStrategy.SKIP)
                    }
                ) {
                    Text("Skip existing birthdays")
                }
                
                // Overwrite option
                TextButton(
                    onClick = {
                        onStrategySelected(ConflictStrategy.OVERWRITE)
                    }
                ) {
                    Text("Overwrite existing birthdays")
                }
                
                // Merge option
                TextButton(
                    onClick = {
                        onStrategySelected(ConflictStrategy.MERGE)
                    }
                ) {
                    Text("Merge with existing birthdays")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}