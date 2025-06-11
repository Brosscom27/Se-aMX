package com.example.proyecto.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.proyecto.ui.screens.HomeScreen
import com.example.proyecto.ui.screens.CameraScreen
import com.example.proyecto.ui.screens.HistoryScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("camera") { CameraScreen(navController) }
        composable("history") { HistoryScreen(navController) }
    }
}
