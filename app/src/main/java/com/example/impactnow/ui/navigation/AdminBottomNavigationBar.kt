// File: com/example/impactnow/ui/navigation/AdminBottomNavigationBar.kt

package com.example.impactnow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AdminBottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Post Opportunity", Icons.Filled.Add, Screen.AdminPost.route),
        BottomNavItem("Posted Opportunities", Icons.Filled.List, Screen.AdminPosted.route),
        BottomNavItem("Applications", Icons.Filled.Apps, Screen.AdminApplications.route) // New Navigation Item
    )

    NavigationBar {
        // Get the current backstack entry to determine the selected screen
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Navigate within the same nested graph
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) }
            )
        }
    }
}
