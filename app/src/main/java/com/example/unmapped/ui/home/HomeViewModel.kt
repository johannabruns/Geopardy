package com.example.unmapped.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.unmapped.data.Player

/**
 * Repräsentiert den UI-Zustand des Home-Bildschirms.
 *
 * @property totalShameScore Der Gesamtpunktestand des Spielers.
 * @property mainPlayer Das Objekt des Hauptspielers.
 */
data class HomeUiState(
    val totalShameScore: Int = 0,
    val mainPlayer: Player? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val playerRepository = PlayerRepository(application)
    private val profilePrefs = application.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMainPlayerAndScore()
    }

    /**
     * Löst eine manuelle Aktualisierung der Spielerdaten aus.
     */
    fun refreshData() {
        loadMainPlayerAndScore()
    }

    /**
     * Lädt die Daten des Hauptspielers (Name, Bild, Punktestand) aus dem Repository
     * und aktualisiert den UI-Zustand.
     */
    private fun loadMainPlayerAndScore() {
        viewModelScope.launch {
            val mainPlayerId = profilePrefs.getString("main_player_id", null)
            val player = mainPlayerId?.let { playerRepository.getPlayerById(it) }

            // Aktualisiert den Zustand nur, wenn ein gültiger Spieler gefunden wurde.
            if (player != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        mainPlayer = player,
                        totalShameScore = player.totalShameScore
                    )
                }
            }
        }
    }
}