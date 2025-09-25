package com.example.unmapped.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Definiert die möglichen Startzustände der App.
 * Eine Sealed Class ist ideal, um einen festen Satz von Zuständen abzubilden.
 */
sealed class StartDestinationState {
    object Loading : StartDestinationState()         // Die App prüft noch, welcher Screen gezeigt werden soll.
    object Start : StartDestinationState()           // Der Start/Login-Screen soll angezeigt werden.
    object Home : StartDestinationState()            // Der Home-Screen soll direkt angezeigt werden.
    object CreateProfile : StartDestinationState()   // Der Screen zur Profilerstellung ist der erste Screen.
}

/**
 * Das ViewModel für die MainActivity.
 * Seine Hauptaufgabe ist es, den Start-Screen der App zu ermitteln und bereitzustellen.
 *
 * @param application Die Anwendungsinstanz.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    // Interner, veränderbarer StateFlow für den Zustand.
    private val _startDestinationState = MutableStateFlow<StartDestinationState>(StartDestinationState.Loading)
    // Öffentlicher, nur lesbarer StateFlow, den die UI beobachten kann.
    val startDestinationState: StateFlow<StartDestinationState> = _startDestinationState.asStateFlow()

    init {
        // In dieser vereinfachten Version wird der Startzustand direkt auf "Start" gesetzt.
        // In einer komplexeren App könnte hier geprüft werden, ob ein Benutzer bereits eingeloggt ist.
        _startDestinationState.value = StartDestinationState.Start
    }
}