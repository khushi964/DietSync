package com.example.dietsync.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dietsync.ui.screens.*

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash")  {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("welcome") {
            WelcomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable("login") {
            LoginScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable("signup") {
            SignupScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable("main") {
            MainScreen(navController)
        }
        composable("day/{day}") { backStackEntry ->
            val day = backStackEntry.arguments?.getString("day") ?: "Unknown"
            DayScreen(day)
        }
    }
}