package com.example.unmapped.ui.recommended

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.BadgeManager
import com.example.unmapped.data.LocationRepository
import com.example.unmapped.data.RoundResult
import com.example.unmapped.data.SettingsManager
import com.example.unmapped.data.getLocationsForMap
import com.example.unmapped.utils.GeoUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Repräsentiert den UI-Zustand für ein Spiel auf einer empfohlenen Karte.
 */
data class RecommendedMapState(
    val isLoading: Boolean = true,
    val gameRounds: List<RecommendedMapRound> = emptyList(),
    val currentRoundIndex: Int = 0,
    val isGameFinished: Boolean = false,
    val formattedTime: String = "02:00",
    val showExitDialog: Boolean = false,
    val isMovementAllowed: Boolean = true,
    val forceGuess: Boolean = false
)

/**
 * Repräsentiert eine einzelne Runde in einem "Recommended Map"-Spiel.
 */
data class RecommendedMapRound(
    val targetLocation: LatLng,
    var guessLocation: LatLng? = null,
    var result: RoundResult? = null
)

class RecommendedViewModel(
    application: Application,
    val mapId: String
) : AndroidViewModel(application) {

    private val _gameState = MutableStateFlow(RecommendedMapState())
    val gameState = _gameState.asStateFlow()

    private val locationRepository = LocationRepository(application)
    private val settingsManager = SettingsManager(application) // Hinzugefügt

    private var timerJob: Job? = null
    private var remainingTimeInMillis: Long = 0
    private var currentTimerDuration: Long = SettingsManager.DEFAULT_TIMER_DURATION.toLong() // Hinzugefügt

    /**
     * Initialisiert ein neues Spiel für die spezifische empfohlene Karte.
     * Filtert bereits korrekt gespielte Orte heraus.
     */
    fun startNewGame() {
        viewModelScope.launch {
            _gameState.update { RecommendedMapState(isLoading = true) }
            currentTimerDuration = settingsManager.timerDuration.first().toLong()

            // Lädt alle Orte, die zu dieser Karten-ID gehören.
            val allLocations = getLocationsForMap(mapId)
            // Filtert die Orte heraus, die der Spieler bereits korrekt erraten hat.
            val unplayedLocations = locationRepository.filterUnplayedLocations(mapId, allLocations)
            val gameRounds = unplayedLocations.shuffled().map { RecommendedMapRound(targetLocation = it) }

            _gameState.update {
                it.copy(
                    isLoading = false,
                    gameRounds = gameRounds,
                    currentRoundIndex = 0,
                    isGameFinished = false
                )
            }
            // Startet den Timer nur, wenn es noch ungespielte Orte gibt.
            if (gameRounds.isNotEmpty()) {
                startTimer()
            }
        }
    }

    /**
     * Verarbeitet die Schätzung des Spielers und das Ergebnis der Runde.
     */
    fun submitGuess(guess: LatLng, distance: Double, score: Int) {
        pauseTimer()
        val timeTaken = ((currentTimerDuration - remainingTimeInMillis) / 1000).toInt()
        val currentRound = _gameState.value.gameRounds[_gameState.value.currentRoundIndex]
        val context = getApplication<Application>().applicationContext

        viewModelScope.launch {
            val actualInfo = GeoUtils.getInfoFromLatLng(context, currentRound.targetLocation)
            val guessInfo = GeoUtils.getInfoFromLatLng(context, guess)

            val result = RoundResult(
                distanceInMeters = distance,
                shameScore = score,
                timeTakenSeconds = timeTaken,
                actualLocation = currentRound.targetLocation,
                guessLocation = guess,
                actualLocationInfo = actualInfo,
                guessLocationInfo = guessInfo
            )

            val updatedRounds = _gameState.value.gameRounds.toMutableList()
            updatedRounds[_gameState.value.currentRoundIndex] = currentRound.copy(
                guessLocation = guess,
                result = result
            )
            _gameState.update { it.copy(gameRounds = updatedRounds, forceGuess = false) }

            // Verarbeitet das Ergebnis speziell für die "Mastery"-Badges der empfohlenen Karten.
            BadgeManager.processRecommendedMapResult(context, result, mapId)
        }
    }

    /**
     * Wechselt zur nächsten Runde oder beendet das Spiel.
     */
    fun nextRound() {
        val nextRoundIndex = _gameState.value.currentRoundIndex + 1
        if (nextRoundIndex < _gameState.value.gameRounds.size) {
            _gameState.update { it.copy(currentRoundIndex = nextRoundIndex, isMovementAllowed = true, forceGuess = false) }
            startTimer()
        } else {
            _gameState.update { it.copy(isGameFinished = true) }
        }
    }

    /**
     * Startet den Countdown-Timer.
     */
    private fun startTimer(durationMillis: Long = currentTimerDuration) { // Verwendet die dynamische Dauer
        timerJob?.cancel()
        remainingTimeInMillis = durationMillis
        _gameState.update { it.copy(formattedTime = formatMillis(remainingTimeInMillis)) }

        timerJob = viewModelScope.launch {
            while (remainingTimeInMillis > 0) {
                delay(1000)
                remainingTimeInMillis -= 1000
                _gameState.update { it.copy(formattedTime = formatMillis(remainingTimeInMillis)) }
            }
            _gameState.update { it.copy(forceGuess = true) }
        }
    }

    /**
     * Hält den Timer an.
     */
    fun pauseTimer() {
        timerJob?.cancel()
    }

    /**
     * Setzt den Timer mit der verbleibenden Zeit fort.
     */
    fun resumeTimer() {
        if (remainingTimeInMillis > 0 && timerJob?.isActive != true && !_gameState.value.forceGuess) {
            startTimer(remainingTimeInMillis)
        }
    }

    /**
     * Formatiert Millisekunden in einen "mm:ss"-String.
     */
    private fun formatMillis(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Wird aufgerufen, wenn der Benutzer versucht, das Spiel zu verlassen.
     */
    fun onExitAttempt() {
        pauseTimer()
        _gameState.update { it.copy(showExitDialog = true) }
    }

    /**
     * Wird aufgerufen, wenn der Benutzer den "Verlassen"-Dialog abbricht.
     */
    fun onExitDismissed() {
        _gameState.update { it.copy(showExitDialog = false) }
        resumeTimer()
    }

    /**
     * Wird aufgerufen, wenn der Benutzer das Verlassen bestätigt.
     */
    fun onExitConfirmed() {
        timerJob?.cancel()
        _gameState.update { it.copy(showExitDialog = false, isGameFinished = true) }
    }
}

/**
 * Eine Factory-Klasse, die [RecommendedViewModel]-Instanzen erstellt.
 * Sie ist notwendig, um dem ViewModel sowohl den `Application`-Kontext als auch die `mapId` zu übergeben.
 */
class RecommendedViewModelFactory(
    private val application: Application,
    private val mapId: String
) : ViewModelProvider.Factory {
    /**
     * Erstellt eine neue Instanz des ViewModels.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecommendedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecommendedViewModel(application, mapId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}