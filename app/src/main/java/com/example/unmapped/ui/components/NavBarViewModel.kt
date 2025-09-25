package com.example.unmapped.ui.components

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.Player
import com.example.unmapped.data.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Repräsentiert den UI-Zustand für die BottomNavBar.
 *
 * @property mainPlayer Das Spieler-Objekt des aktuell angemeldeten Hauptspielers.
 */
data class NavBarUiState(
    val mainPlayer: Player? = null
)

/**
 * Eine Factory-Klasse zur Erstellung von [NavBarViewModel]-Instanzen.
 * Dies ist notwendig, um dem ViewModel den `Application`-Kontext zu übergeben.
 */
class NavBarViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    /**
     * Erstellt eine neue Instanz des ViewModels.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NavBarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NavBarViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Der ViewModel für die BottomNavBar.
 * Seine Aufgabe ist es, die Daten des Hauptspielers zu laden und für die UI bereitzustellen.
 */
class NavBarViewModel(application: Application) : AndroidViewModel(application) {
    private val playerRepository = PlayerRepository(application)
    private val profilePrefs = application.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(NavBarUiState())
    val uiState: StateFlow<NavBarUiState> = _uiState.asStateFlow()

    init {
        loadMainPlayer()
    }

    /**
     * Löst ein erneutes Laden der Spielerdaten aus.
     * Nützlich, um die Navigationsleiste zu aktualisieren, nachdem sich Profildaten geändert haben.
     */
    fun refreshData() {
        loadMainPlayer()
    }

    /**
     * Lädt den Hauptspieler aus dem Repository.
     * Liest die ID des Hauptspielers aus den SharedPreferences und holt dann das vollständige Spielerobjekt.
     */
    private fun loadMainPlayer() {
        viewModelScope.launch {
            val mainPlayerId = profilePrefs.getString("main_player_id", null)
            val player = mainPlayerId?.let { playerRepository.getPlayerById(it) }
            _uiState.update { it.copy(mainPlayer = player) }
        }
    }
}