package com.example.impactnow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.impactnow.screens.ApplyScreen
import com.example.impactnow.screens.DetailScreen
import com.example.impactnow.screens.HomeScreen
import com.example.impactnow.screens.ProfileScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Apply : Screen("apply/{opportunityId}") {
        fun createRoute(opportunityId: String) = "apply/$opportunityId"
    }
    object Detail : Screen("detail/{opportunityId}") {
        fun createRoute(opportunityId: String) = "detail/$opportunityId"
    }
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(
            route = Screen.Apply.route,
            arguments = ScreenArguments.applyArguments()
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId")
            ApplyScreen(opportunityId = opportunityId, navController = navController)
        }
        composable(
            route = Screen.Detail.route,
            arguments = ScreenArguments.detailArguments()
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId")
            DetailScreen(opportunityId = opportunityId, navController = navController)
        }
    }
}
