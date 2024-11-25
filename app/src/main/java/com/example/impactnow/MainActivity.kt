// File: com/example/impactnow/MainActivity.kt

package com.example.impactnow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.impactnow.ui.navigation.AdminBottomNavigationBar
import com.example.impactnow.ui.navigation.BottomNavigationBar
import com.example.impactnow.ui.navigation.NavGraph
import com.example.impactnow.ui.navigation.Screen
import com.example.impactnow.ui.theme.ImpactNowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImpactNowTheme {
                val navController = rememberNavController()
                MainScreen(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    // Observe the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if Bottom Bar should be shown and which one
    val (showBottomBar, bottomBarType) = when {
        // Check if the current route is part of the studentFlow
        currentRoute?.startsWith("home") == true ||
                currentRoute?.startsWith("applications") == true ||
                currentRoute?.startsWith("saved") == true ||
                currentRoute?.startsWith("profile") == true ||
                currentRoute?.startsWith("apply/") == true -> Pair(true, "student")

        // Check if the current route is part of the adminFlow
        currentRoute?.startsWith("adminPost") == true ||
                currentRoute?.startsWith("adminPosted") == true -> Pair(true, "admin")

        else -> Pair(false, "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ImpactNow") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (showBottomBar) {
                when (bottomBarType) {
                    "student" -> BottomNavigationBar(navController = navController)
                    "admin" -> AdminBottomNavigationBar(navController = navController)
                    else -> {}
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
