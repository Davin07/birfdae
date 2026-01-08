package com.birthdayreminder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Upcoming
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.birthdayreminder.ui.components.LuminaGlassCard
import com.birthdayreminder.ui.navigation.BirthdayNavigation
import com.birthdayreminder.ui.screens.AddEditBirthdayScreen
import com.birthdayreminder.ui.screens.BackupScreen
import com.birthdayreminder.ui.screens.BirthdayListScreen
import com.birthdayreminder.ui.screens.CalendarScreen
import com.birthdayreminder.ui.screens.NotificationSettingsScreen
import com.birthdayreminder.ui.screens.SearchScreen
import com.birthdayreminder.ui.theme.LuminaPrimaryGradient

/**
 * Main app composable that sets up navigation and bottom navigation bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayApp(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom navigation on main screens
            if (currentDestination?.route in
                listOf(
                    BirthdayNavigation.BIRTHDAY_LIST,
                    BirthdayNavigation.CALENDAR,
                    BirthdayNavigation.SEARCH,
                    BirthdayNavigation.NOTIFICATION_SETTINGS,
                )
            ) {
                BirthdayBottomNavigation(
                    navController = navController,
                    currentDestination = currentDestination?.route,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BirthdayNavigation.BIRTHDAY_LIST,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            composable(BirthdayNavigation.BIRTHDAY_LIST) {
                BirthdayListScreen(
                    onNavigateToAddBirthday = {
                        navController.navigate(BirthdayNavigation.ADD_EDIT_BIRTHDAY)
                    },
                    onNavigateToEditBirthday = { birthdayId ->
                        navController.navigate(BirthdayNavigation.createAddEditBirthdayRoute(birthdayId))
                    },
                )
            }

            composable(BirthdayNavigation.CALENDAR) {
                CalendarScreen(
                    onBirthdayClick = { birthday ->
                        navController.navigate(BirthdayNavigation.createAddEditBirthdayRoute(birthday.id))
                    },
                )
            }

            composable(BirthdayNavigation.SEARCH) {
                SearchScreen(
                    onNavigateToEditBirthday = { birthdayId ->
                        navController.navigate(BirthdayNavigation.createAddEditBirthdayRoute(birthdayId))
                    }
                )
            }

            composable(BirthdayNavigation.ADD_EDIT_BIRTHDAY) {
                AddEditBirthdayScreen(
                    birthdayId = null,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(BirthdayNavigation.ADD_EDIT_BIRTHDAY_WITH_ID) { backStackEntry ->
                val birthdayId = backStackEntry.arguments?.getString("birthdayId")?.toLongOrNull()
                AddEditBirthdayScreen(
                    birthdayId = birthdayId,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(BirthdayNavigation.NOTIFICATION_SETTINGS) {
                NotificationSettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController,
                )
            }

            composable(BirthdayNavigation.BACKUP) {
                BackupScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/**
 * Bottom navigation bar for main app screens - Redesigned as a floating glass bar
 */
@Composable
private fun BirthdayBottomNavigation(
    navController: NavHostController,
    currentDestination: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        LuminaGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calendar
                BottomNavItem(
                    icon = Icons.Rounded.CalendarMonth,
                    label = "Calendar",
                    selected = currentDestination == BirthdayNavigation.CALENDAR,
                    onClick = { navigateTo(navController, BirthdayNavigation.CALENDAR) },
                    modifier = Modifier.weight(1f)
                )

                // Upcoming (Home)
                BottomNavItem(
                    icon = Icons.Rounded.Upcoming,
                    label = "Upcoming",
                    selected = currentDestination == BirthdayNavigation.BIRTHDAY_LIST,
                    onClick = { navigateTo(navController, BirthdayNavigation.BIRTHDAY_LIST) },
                    modifier = Modifier.weight(1f)
                )

                // Add (Faux FAB)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = (-20).dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(LuminaPrimaryGradient))
                                .clickable { navigateTo(navController, BirthdayNavigation.ADD_EDIT_BIRTHDAY) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Search
                BottomNavItem(
                    icon = Icons.Rounded.Search,
                    label = "Search",
                    selected = currentDestination == BirthdayNavigation.SEARCH,
                    onClick = { navigateTo(navController, BirthdayNavigation.SEARCH) },
                    modifier = Modifier.weight(1f)
                )

                // Settings
                BottomNavItem(
                    icon = Icons.Rounded.Settings,
                    label = "Settings",
                    selected = currentDestination == BirthdayNavigation.NOTIFICATION_SETTINGS,
                    onClick = { navigateTo(navController, BirthdayNavigation.NOTIFICATION_SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun navigateTo(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Individual item for the bottom navigation
 */
@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = if (selected) {
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            } else {
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/**
 * Placeholder screen for development
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}
