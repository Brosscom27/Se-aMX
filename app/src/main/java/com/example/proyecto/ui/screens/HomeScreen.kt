package com.example.proyecto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.R
import androidx.compose.foundation.shape.CircleShape

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logotipo",
                    modifier = Modifier.size(290.dp) // Tamaño logo
                )
                Spacer(modifier = Modifier.height(100.dp))

                Button(
                    onClick = { navController.navigate("camera") },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(120.dp) // Tamaño botón
                ) {
                    Text("Iniciar", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    )
}

