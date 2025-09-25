package com.example.unmapped.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Eine Factory-Klasse zur Erstellung von [HomeViewModel]-Instanzen.
 * Dies ist notwendig, um dem ViewModel den `Application`-Kontext zu übergeben.
 */
class HomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    /**
     * Erstellt eine neue Instanz des ViewModels.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}