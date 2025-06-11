package com.example.proyecto.data

// Objeto singleton para almacenar el historial
object HistoryRepository {
    // Lista para los elementos del historial
    private val historyList = mutableListOf<String>()

    // Devuelve el historial actual como una lista inmutable
    fun getHistory(): List<String> = historyList

    // Agregar un nuevo elemento (letra) al historial
    fun addToHistory(letra: String) {
        historyList.add(letra)
    }

    // Reemplaza el historial con una nueva lista
    fun setHistory(items: List<String>) {
        historyList.clear()           // Se borra el historial actual
        historyList.addAll(items)     // Se agregan los nuevos elementos
    }

    // Limpia el historial completo
    fun clearHistory() {
        historyList.clear()
    }
}

