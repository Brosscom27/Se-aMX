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
    val historyItems = remember { HistoryRepository.getHistory() }
    var items by remember { mutableStateOf(historyItems) }

    Scaffold(
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val updatedItems = items.toMutableList()
                    updatedItems.removeLastOrNull()
                    items = updatedItems
                    HistoryRepository.setHistory(updatedItems)
                },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar último")
            }
        },
        bottomBar = {
            Column {
                Button(
                    onClick = {
                        HistoryRepository.clearHistory()
                        items = emptyList()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Borrar todo")
                }
                BottomNavigationBar(navController)
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items) { item ->
                        Text(text = item, style = MaterialTheme.typography.bodyLarge)
                        Divider()
                    }
                }
            }
        }
    )
}
