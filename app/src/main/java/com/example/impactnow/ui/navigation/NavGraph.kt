package com.example.impactnow.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.impactnow.screens.*
import com.example.impactnow.ui.auth.*
import com.example.impactnow.ui.admin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object MainFlow : Screen("mainFlow")

    // Student Screens
    object Home : Screen("home")
    object Applications : Screen("applications")
    object Saved : Screen("saved")
    object Profile : Screen("profile")
    object Apply : Screen("apply/{opportunityId}") {
        fun createRoute(opportunityId: String) = "apply/$opportunityId"
    }

    // Admin Screens
    object AdminPosted : Screen("adminPosted")
    object AdminPost : Screen("adminPost")
    object AdminApplications : Screen("adminApplications") // New Screen

}

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
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

        // Main Flow - Role-Based Navigation
        composable(Screen.MainFlow.route) {
            RoleBasedNavigation(navController)
        }

        // Student Navigation Graph
        navigation(startDestination = Screen.Home.route, route = "studentFlow") {
            composable(Screen.Home.route) {
                StudentHomeScreen(navController)
            }
            composable(Screen.Applications.route) {
                ApplicationsScreen(navController)
            }
            composable(Screen.Saved.route) {
                SavedOrganizationsScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
            composable(
                route = Screen.Apply.route,
                arguments = listOf(navArgument("opportunityId") { defaultValue = "" })
            ) { backStackEntry ->
                val opportunityId = backStackEntry.arguments?.getString("opportunityId")
                ApplyScreen(opportunityId = opportunityId, navController = navController)
            }
        }

        // Admin Navigation Graph
        navigation(startDestination = Screen.AdminPost.route, route = "adminFlow") {
            composable(Screen.AdminPost.route) {
                PostOpportunityScreen(navController)
            }
            composable(Screen.AdminPosted.route) {
                OrganizationsPostedScreen(navController)
            }
            composable(Screen.AdminApplications.route) { // New Composable
                AdminApplicationsScreen(navController)
            }
        }
    }
}

// Role-Based Navigation
@Composable
fun RoleBasedNavigation(navController: NavHostController) {
    var role by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Fetch user role from Firestore
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    role = document.getString("role") ?: "Student" // Default to Student
                }
                .addOnFailureListener {
                    role = "Unknown" // Handle error scenario
                }
        } else {
            role = "Unknown"
        }
    }

    when (role) {
        null -> {
            // Show loading state while waiting for role determination
            LoadingScreen()
        }
        "Admin" -> {
            // Navigate to adminFlow
            LaunchedEffect(role) {
                navController.navigate("adminFlow") {
                    popUpTo(Screen.MainFlow.route) { inclusive = true }
                }
            }
        }
        "Student" -> {
            // Navigate to studentFlow
            LaunchedEffect(role) {
                navController.navigate("studentFlow") {
                    popUpTo(Screen.MainFlow.route) { inclusive = true }
                }
            }
        }
        else -> {
            // Handle unknown or error scenarios
            ErrorScreen(navController)
        }
    }
}

// Loading Screen
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// Error Screen
@Composable
fun ErrorScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("An error occurred. Please try again.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate(Screen.Login.route) }) {
                Text("Go to Login")
            }
        }
    }
}
