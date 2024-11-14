package com.example.impactnow.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object ScreenArguments {
    fun applyArguments() = listOf(
        navArgument("opportunityId") {
            type = NavType.StringType
            nullable = true
        }
    )

    fun detailArguments() = listOf(
        navArgument("opportunityId") {
            type = NavType.StringType
            nullable = true
        }
    )
}
