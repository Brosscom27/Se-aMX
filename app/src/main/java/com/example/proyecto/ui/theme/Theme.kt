package com.example.proyecto.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF004a75),    // Azul más oscuro para dark
    secondary = Color(0xFF004a75),  // Consistencia
    background = Color(0xFF001f2f), // Azul muy oscuro para fondo
    surface = Color(0xFF001f2f),    // Azul muy oscuro para superficies
    onPrimary = Color(0xFFFFFFFF),  // Blanco para texto sobre primary
    onSecondary = Color(0xFFFFFFFF),// Blanco para texto sobre secondary
    onBackground = Color(0xFF000000),// Negro para texto sobre fondo claro
    onSurface = Color(0xFF000000)    // Negro para texto sobre superficie
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0175bb),    // Azul oscuro (botón)
    secondary = Color(0xFF0175bb),  // También azul oscuro para consistencia
    background = Color(0xFFd6eefc), // Azul claro (fondo)
    surface = Color(0xFFd6eefc),    // También azul claro para superficies
    onPrimary = Color(0xFFFFFFFF),  // Blanco para texto sobre primary
    onSecondary = Color(0xFFFFFFFF),// Blanco para texto sobre secondary
    onBackground = Color(0xFF000000),// Negro para texto sobre fondo claro
    onSurface = Color(0xFF000000)    // Negro para texto sobre superficie
)

@Composable
fun ProyectoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}