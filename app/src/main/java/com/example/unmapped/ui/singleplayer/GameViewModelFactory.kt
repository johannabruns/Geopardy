package com.example.unmapped.ui.singleplayer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Eine Factory-Klasse zur Erstellung von [GameViewModel]-Instanzen.
 * Notwendig, um dem ViewModel den `Application`-Kontext zu übergeben.
 */
class GameViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    /**
     * Erstellt eine neue Instanz des ViewModels.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}