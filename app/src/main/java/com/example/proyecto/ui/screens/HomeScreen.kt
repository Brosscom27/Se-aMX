package com.example.proyecto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.R

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        containerColor = Color.Transparent, // color del contenedor transparente para fondo degradado
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF47baff ), // Azul oscuro
                                Color(0xFFd6eefc)  // Azul claro
                            )
                        )
                    )
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo), // Imagen del logo
                        contentDescription = "Logotipo",
                        modifier = Modifier.size(290.dp)
                    )

                    Spacer(modifier = Modifier.height(100.dp))

                    Button(
                        onClick = { navController.navigate("camera") }, // Acción para navegar
                        shape = CircleShape,
                        modifier = Modifier.size(120.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0175bb),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(8.dp) // Sombra del botón
                    ) {
                        Text("INICIO", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    )
}


