package com.example.unmapped.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Erstellt eine DataStore-Instanz als Erweiterung des Contexts, um sie App-weit verfügbar zu machen.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Verwaltet App-Einstellungen wie den Dark Mode oder die Timer-Dauer mithilfe von Jetpack DataStore.
 * DataStore bietet eine sichere und asynchrone Alternative zu SharedPreferences.
 *
 * @param context Der Anwendungskontext.
 */
class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        // Schlüssel für die verschiedenen Einstellungen.
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
        val TIMER_DURATION_KEY = intPreferencesKey("timer_duration_millis")

        // Standardwert für die Timer-Dauer (2 Minuten).
        const val DEFAULT_TIMER_DURATION = 120000
    }

    /**
     * Ein Flow, der den aktuellen Zustand des Dark Modes repräsentiert.
     * Die UI kann diesen Flow beobachten, um sich bei Änderungen automatisch zu aktualisieren.
     */
    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false // Gibt false zurück, wenn der Wert nicht gesetzt ist.
    }

    /**
     * Speichert den Zustand des Dark Modes. Dies ist eine suspend-Funktion,
     * da DataStore-Operationen asynchron sind.
     *
     * @param isDarkMode Der neue Zustand, der gespeichert werden soll.
     */
    suspend fun setDarkMode(isDarkMode: Boolean) {
        dataStore.edit { settings ->
            settings[DARK_MODE_KEY] = isDarkMode
        }
    }

    /**
     * Ein Flow für die Dauer des Timers in Millisekunden.
     */
    val timerDuration: Flow<Int> = dataStore.data.map { preferences ->
        preferences[TIMER_DURATION_KEY] ?: DEFAULT_TIMER_DURATION
    }

    /**
     * Speichert die Dauer des Timers.
     *
     * @param durationMillis Die neue Dauer in Millisekunden.
     */
    suspend fun setTimerDuration(durationMillis: Int) {
        dataStore.edit { settings ->
            settings[TIMER_DURATION_KEY] = durationMillis
        }
    }
}