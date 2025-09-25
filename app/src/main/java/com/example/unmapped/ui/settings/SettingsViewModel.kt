package com.example.unmapped.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Definiert die verschiedenen Informationsthemen, die im Overlay angezeigt werden können.
 */
enum class InfoTopic(val title: String) {
    PROJECT_IDEA("Projektidee & Funktionen"),
    TECHNICAL("Technische Umsetzung"),
    HOW_TO_PLAY("How To Play"),
    UX_UI("User Experience & Design")
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    // Stellt den Dark-Mode-Status als StateFlow zur Verfügung.
    val isDarkMode: StateFlow<Boolean> = settingsManager.isDarkMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Stellt die Timer-Dauer als StateFlow zur Verfügung.
    val timerDuration: StateFlow<Int> = settingsManager.timerDuration.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsManager.DEFAULT_TIMER_DURATION
    )

    // Hält den Zustand, welches Info-Thema aktuell im Overlay angezeigt wird.
    private val _shownInfoTopic = MutableStateFlow<InfoTopic?>(null)
    val shownInfoTopic = _shownInfoTopic.asStateFlow()

    /**
     * Speichert die Einstellung für den Dark Mode.
     * @param isDarkMode True, wenn der Dark Mode aktiviert werden soll.
     */
    fun setDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            settingsManager.setDarkMode(isDarkMode)
        }
    }

    /**
     * Speichert die ausgewählte Timer-Dauer.
     * @param durationMillis Die Dauer in Millisekunden.
     */
    fun setTimerDuration(durationMillis: Int) {
        viewModelScope.launch {
            settingsManager.setTimerDuration(durationMillis)
        }
    }

    /**
     * Zeigt das Info-Overlay für ein bestimmtes Thema an.
     * @param topic Das anzuzeigende Thema.
     */
    fun showInfoTopic(topic: InfoTopic) {
        _shownInfoTopic.value = topic
    }

    /**
     * Verbirgt das Info-Overlay.
     */
    fun hideInfoTopic() {
        _shownInfoTopic.value = null
    }
}

/**
 * Eine Factory-Klasse zur Erstellung von [SettingsViewModel]-Instanzen.
 */
class SettingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    /**
     * Erstellt eine neue Instanz des ViewModels.
     */
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}