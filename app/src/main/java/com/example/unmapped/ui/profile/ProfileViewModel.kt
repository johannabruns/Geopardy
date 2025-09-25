package com.example.unmapped.ui.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Repräsentiert den UI-Zustand des Profil-Bildschirms.
 * @property mainPlayer Das Objekt des Hauptspielers.
 * @property badges Eine Map, die Badge-Definitionen mit dem jeweiligen Fortschritt verknüpft.
 * @property gameStats Die aggregierten Spielstatistiken des Spielers.
 */
data class ProfileUiState(
    val mainPlayer: Player? = null,
    val badges: Map<BadgeDefinition, BadgeProgress> = emptyMap(),
    val gameStats: GameStats = GameStats()
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val profilePrefs = application.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    private val playerRepository = PlayerRepository(application)
    private val badgeRepository = BadgeRepository(application)
    private val statsRepository = StatsRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    companion object {
        private const val MAIN_PLAYER_ID_KEY = "main_player_id"
    }

    init {
        // Lädt alle für das Profil benötigten Daten.
        loadMainPlayer()
        loadBadges()
        loadStats()
    }

    /**
     * Lädt die Daten des Hauptspielers aus dem Repository.
     */
    private fun loadMainPlayer() {
        viewModelScope.launch {
            val mainPlayerId = profilePrefs.getString(MAIN_PLAYER_ID_KEY, null)
            val player = mainPlayerId?.let { playerRepository.getPlayerById(it) }
            _uiState.update { it.copy(mainPlayer = player) }
        }
    }

    /**
     * Lädt die Badges inklusive ihres Fortschritts aus dem Repository.
     */
    private fun loadBadges() {
        viewModelScope.launch {
            val badges = badgeRepository.getAllBadgesWithProgress()
            _uiState.update { it.copy(badges = badges) }
        }
    }

    /**
     * Lädt die aggregierten Spielstatistiken aus dem Repository.
     */
    private fun loadStats() {
        viewModelScope.launch {
            val stats = statsRepository.getStats()
            _uiState.update { it.copy(gameStats = stats) }
        }
    }

    /**
     * Aktualisiert den Namen des Hauptspielers.
     * @param newName Der neue Name.
     */
    fun updateUserName(newName: String) {
        val nameToSave = newName.ifBlank { "PlayerOne" }
        _uiState.value.mainPlayer?.let { currentPlayer ->
            val updatedPlayer = currentPlayer.copy(name = nameToSave)
            viewModelScope.launch {
                playerRepository.updatePlayer(updatedPlayer)
                _uiState.update { it.copy(mainPlayer = updatedPlayer) }
            }
        }
    }

    /**
     * Aktualisiert die Profilbild-URI des Hauptspielers.
     * @param uri Die neue Bild-URI.
     */
    fun updateProfileImageUri(uri: Uri?) {
        _uiState.value.mainPlayer?.let { currentPlayer ->
            val updatedPlayer = currentPlayer.copy(imageUri = uri?.toString())
            viewModelScope.launch {
                playerRepository.updatePlayer(updatedPlayer)
                _uiState.update { it.copy(mainPlayer = updatedPlayer) }
            }
        }
    }
}