package com.example.proyecto.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        // Navegación para la pantalla de inicio
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = false,
            onClick = { navController.navigate("home") } // Navega a la ruta "home"
        )
        // Elemento de navegación para la pantalla de cámara
        NavigationBarItem(
            icon = { Icon(Icons.Default.Camera, contentDescription = "Cámara") },
            label = { Text("Cámara") },
            selected = false,
            onClick = { navController.navigate("camera") } // Navega a la ruta "camera"
        )
        // Elemento de navegación para la pantalla de historial
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
            label = { Text("Historial") },
            selected = false, // No se indica selección explícita
            onClick = { navController.navigate("history") } // Navega a la ruta "history"
        )
    }
}

