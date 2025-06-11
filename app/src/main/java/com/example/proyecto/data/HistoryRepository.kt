package com.example.proyecto.data

object HistoryRepository {
    private val historyList = mutableListOf<String>()

    fun getHistory(): List<String> = historyList

    fun addToHistory(letra: String) {
        historyList.add(letra)
    }

    fun setHistory(items: List<String>) {
        historyList.clear()
        historyList.addAll(items)
    }

    fun clearHistory() {
        historyList.clear()
    }
}
