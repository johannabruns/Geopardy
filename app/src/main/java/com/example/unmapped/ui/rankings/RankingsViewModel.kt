package com.example.unmapped.ui.rankings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.Player
import com.example.unmapped.data.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Repräsentiert den UI-Zustand des Rankings-Bildschirms.
 * @property players Die Liste der Spieler, sortiert nach ihrem Rang.
 * @property isLoading True, während die Daten geladen werden.
 */
data class RankingsUiState(
    val players: List<Player> = emptyList(),
    val isLoading: Boolean = true
)

class RankingsViewModel(application: Application) : AndroidViewModel(application) {
    private val playerRepository = PlayerRepository(application)
    private val _uiState = MutableStateFlow(RankingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRankings()
    }

    /**
     * Lädt alle Spieler aus dem Repository und sortiert sie absteigend nach ihrem Punktestand,
     * um das "Loserboard" zu erstellen (höchste Punktzahl = bester/schlechtester Rang).
     */
    fun loadRankings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allPlayers = playerRepository.getAllPlayers()
            // Sortiert die Spieler absteigend nach ihrem ShameScore.
            val sortedPlayers = allPlayers.sortedByDescending { it.totalShameScore }
            _uiState.update { RankingsUiState(players = sortedPlayers, isLoading = false) }
        }
    }
}

/**
 * Eine Factory-Klasse zur Erstellung von [RankingsViewModel]-Instanzen.
 */
class RankingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    /**
     * Erstellt eine neue Instanz des ViewModels.
     */
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RankingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}