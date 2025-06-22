package com.example.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.data.HistoryRepository
import com.example.proyecto.ui.navigation.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    // Se obtiene el historial
    val historyItems = remember { HistoryRepository.getHistory() }
    // Se guarda el estado para permitir actualizaciones
    var items by remember { mutableStateOf(historyItems) }

    // Estructura principal de la pantalla
    Scaffold(
        // Fondo azul claro para toda la pantalla
        containerColor = Color(0xFFd6eefc),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0175bb), // azul de los botones
                    titleContentColor = Color.White
                )
            )
        },
        // Botón flotante para borrar el último elemento
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val updatedItems = items.toMutableList() // Se crea una copia mutable de la lista
                    updatedItems.removeLastOrNull() // Elimina el último elemento si existe
                    items = updatedItems // Actualiza el estado
                    HistoryRepository.setHistory(updatedItems) // Actualiza el historial
                },
                containerColor = Color(0xFF0175bb), // azul de los botones
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar último")
            }
        },
        // Barra inferior con botón para borrar todo
        bottomBar = {
            Column {
                Button(
                    onClick = {
                        HistoryRepository.clearHistory() // Limpia el historial
                        items = emptyList() // Limpia la lista local
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0175bb), // azul de los botones
                        contentColor = Color.White
                    )
                ) {
                    Text("Borrar todo")
                }
                BottomNavigationBar(navController)
            }
        },
        // Contenido principal de la pantalla
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Lista para mostrar los ítems del historial
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items) { item ->
                        Text(text = item, style = MaterialTheme.typography.bodyLarge)
                        Divider() // Línea divisoria entre ítems
                    }
                }
            }
        }
    )
}



