package com.birthdayreminder.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange // Changed from CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
// import androidx.navigation.NavDestination.Companion.hierarchy // Removed unused import
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.birthdayreminder.ui.navigation.BirthdayNavigation
import com.birthdayreminder.ui.screens.AddEditBirthdayScreen
import com.birthdayreminder.ui.screens.BackupScreen
import com.birthdayreminder.ui.screens.BirthdayListScreen
import com.birthdayreminder.ui.screens.CalendarScreen
import com.birthdayreminder.ui.screens.NotificationSettingsScreen

/**
 * Main app composable that sets up navigation and bottom navigation bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom navigation on main screens
            if (currentDestination?.route in listOf(
                BirthdayNavigation.BIRTHDAY_LIST,
                BirthdayNavigation.CALENDAR,
                BirthdayNavigation.NOTIFICATION_SETTINGS
            )) {
                BirthdayBottomNavigation(
                    navController = navController,
                    currentDestination = currentDestination?.route
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BirthdayNavigation.BIRTHDAY_LIST,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(BirthdayNavigation.BIRTHDAY_LIST) {
                BirthdayListScreen(
                    onNavigateToAddBirthday = {
                        navController.navigate(BirthdayNavigation.ADD_EDIT_BIRTHDAY)
                    },
                    onNavigateToEditBirthday = { birthdayId ->
                        navController.navigate(BirthdayNavigation.createAddEditBirthdayRoute(birthdayId))
                    }
                )
            }
            
            composable(BirthdayNavigation.CALENDAR) {
                CalendarScreen(
                    onBirthdayClick = { birthday ->
                        navController.navigate(BirthdayNavigation.createAddEditBirthdayRoute(birthday.id))
                    }
                )
            }
            
            composable(BirthdayNavigation.ADD_EDIT_BIRTHDAY) {
                AddEditBirthdayScreen(
                    birthdayId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(BirthdayNavigation.ADD_EDIT_BIRTHDAY_WITH_ID) { backStackEntry ->
                val birthdayId = backStackEntry.arguments?.getString("birthdayId")?.toLongOrNull()
                AddEditBirthdayScreen(
                    birthdayId = birthdayId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(BirthdayNavigation.NOTIFICATION_SETTINGS) {
                NotificationSettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController
                )
            }
            
            composable(BirthdayNavigation.BACKUP) {
                BackupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Bottom navigation bar for main app screens
 */
@Composable
private fun BirthdayBottomNavigation(
    navController: NavHostController,
    currentDestination: String?
) {
    val items = listOf(
        BottomNavItem(
            route = BirthdayNavigation.BIRTHDAY_LIST,
            icon = Icons.Default.List,
            label = "List"
        ),
        BottomNavItem(
            route = BirthdayNavigation.CALENDAR,
            icon = Icons.Default.DateRange, // Changed from CalendarMonth
            label = "Calendar"
        ),
        BottomNavItem(
            route = BirthdayNavigation.NOTIFICATION_SETTINGS,
            icon = Icons.Default.Settings,
            label = "Settings"
        )
    )
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Data class for bottom navigation items
 */
private data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Placeholder screen for development
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}