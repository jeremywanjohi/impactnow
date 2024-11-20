package com.example.impactnow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.impactnow.screens.ApplyScreen
import com.example.impactnow.screens.DetailScreen
import com.example.impactnow.screens.HomeScreen
import com.example.impactnow.ui.auth.LoginScreen
import com.example.impactnow.ui.auth.SignupScreen
import com.example.impactnow.ui.auth.SplashScreen
import com.example.impactnow.ui.admin.AdminScreen

sealed class Screen(val route: String) {
    object Splash :Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Admin : Screen("admin") // Added Admin route
    object Apply : Screen("apply/{opportunityId}") {
        fun createRoute(opportunityId: String) = "apply/$opportunityId"
    }
    object Detail : Screen("detail/{opportunityId}") {
        fun createRoute(opportunityId: String) = "detail/$opportunityId"
    }
}

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    )

    {
        composable(Screen.Splash.route){
            SplashScreen(navController)
        }
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        // Signup Screen
        composable(Screen.Signup.route) {
            SignupScreen(navController)
        }

        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        // Admin Screen
        composable(Screen.Admin.route) { // Added AdminScreen
            AdminScreen(navController)
        }

        // Apply Screen with Opportunity ID argument
        composable(
            route = Screen.Apply.route,
            arguments = listOf(navArgument("opportunityId") { defaultValue = "" })
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId")
            ApplyScreen(opportunityId = opportunityId, navController = navController)
        }

        // Detail Screen with Opportunity ID argument
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("opportunityId") { defaultValue = "" })
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId")
            DetailScreen(opportunityId = opportunityId, navController = navController)
        }
    }
}
