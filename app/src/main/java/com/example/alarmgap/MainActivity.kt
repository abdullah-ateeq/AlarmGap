package com.example.alarmgap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmgap.navigation.Screen
import com.example.alarmgap.ui.about.AboutScreen
import com.example.alarmgap.ui.history.HistoryScreen
import com.example.alarmgap.ui.home.AlarmGapViewModel
import com.example.alarmgap.ui.home.HomeScreen
import com.example.alarmgap.ui.settings.SettingsScreen
import com.example.alarmgap.ui.splash.SplashScreen
import com.example.alarmgap.ui.theme.AlarmGapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AlarmGapViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            AlarmGapTheme(
                themePreference = uiState.themePreference,
                dynamicColor = uiState.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        enterTransition = { if (uiState.animations) fadeIn() else EnterTransition.None },
                        exitTransition = { if (uiState.animations) fadeOut() else ExitTransition.None }
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                onSplashFinished = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateHistory = { navController.navigate(Screen.History.route) },
                                onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                                onNavigateAbout = { navController.navigate(Screen.About.route) }
                            )
                        }

                        composable(Screen.History.route) {
                            HistoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.About.route) {
                            AboutScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
