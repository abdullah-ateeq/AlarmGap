package com.example.alarmgap.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}
