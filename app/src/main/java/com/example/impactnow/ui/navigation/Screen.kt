package com.example.impactnow.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Admin : Screen("admin")
    object Apply : Screen("apply/{opportunityId}") {
        fun createRoute(opportunityId: String) = "apply/$opportunityId"
    }
    object Detail : Screen("detail/{opportunityId}") {
        fun createRoute(opportunityId: String) = "detail/$opportunityId"
    }
}
