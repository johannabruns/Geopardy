package com.example.unmapped.ui.multiplayer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unmapped.data.PlayerRepository

/**
 * Eine Factory-Klasse, die ViewModels für den gesamten Multiplayer-Fluss erstellt.
 * Sie kann sowohl den [MultiplayerSetupViewModel] als auch den [MultiplayerViewModel] instanziieren.
 */
class MultiplayerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    /**
     * Erstellt eine neue Instanz des angeforderten ViewModels.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val playerRepository = PlayerRepository(application)
        return when {
            // Wenn der SetupViewModel angefordert wird:
            modelClass.isAssignableFrom(MultiplayerSetupViewModel::class.java) -> {
                MultiplayerSetupViewModel(application) as T
            }
            // Wenn der Haupt-Game-ViewModel angefordert wird:
            modelClass.isAssignableFrom(MultiplayerViewModel::class.java) -> {
                MultiplayerViewModel(application, playerRepository) as T
            }
            // Wirft einen Fehler, wenn ein unbekannter ViewModel-Typ angefordert wird.
            else -> throw IllegalArgumentException("Unknown ViewModel class for Multiplayer factory: ${modelClass.name}")
        }
    }
}