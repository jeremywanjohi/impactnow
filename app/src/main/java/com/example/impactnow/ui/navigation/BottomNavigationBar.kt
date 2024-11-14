package com.example.impactnow.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.impactnow.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Home to R.drawable.home__1_, // Update to your actual resource names
        Screen.Apply to R.drawable.clipboard,
        Screen.Detail to R.drawable.file,
        Screen.Profile to R.drawable.profile_user
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        tonalElevation = 4.dp, // Optional for shadow
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.height(56.dp) // Make the navigation bar smaller
    ) {
        items.forEach { (screen, icon) ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = screen.route,
                        modifier = Modifier.size(20.dp) // Smaller icon size
                    )
                },
                label = {
                    Text(
                        text = screen.route.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f // Reduce label size slightly
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
