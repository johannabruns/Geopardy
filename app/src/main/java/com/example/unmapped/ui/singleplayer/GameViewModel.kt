package com.example.unmapped.ui.singleplayer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.LocationRepository
import com.example.unmapped.data.RoundResult
import com.example.unmapped.data.SettingsManager
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
 * Repräsentiert den gesamten Zustand eines Einzelspieler-Spiels.
 */
data class GameState(
    val isLoading: Boolean = true,
    val gameRounds: List<GameRound> = emptyList(),
    val currentRoundIndex: Int = 0,
    val isGameFinished: Boolean = false,
    val formattedTime: String = "02:00",
    val showExitDialog: Boolean = false,
    val isMovementAllowed: Boolean = true,
    val forceGuess: Boolean = false
)

/**
 * Repräsentiert eine einzelne Runde in einem Einzelspieler-Spiel.
 */
data class GameRound(
    val targetLocation: LatLng,
    var guessLocation: LatLng? = null,
    var result: RoundResult? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application)
    private val settingsManager = SettingsManager(application)
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private var timerJob: Job? = null
    var remainingTimeInMillis: Long = 0
    private var currentTimerDuration: Long = SettingsManager.DEFAULT_TIMER_DURATION.toLong()

    companion object {
        private const val TOTAL_ROUNDS_STANDARD = 5
        private const val TAG = "GameViewModel"
        private const val MIN_LOADING_TIME_MS = 2500L // Sorgt für eine angenehme Lade-Animation.
    }

    /**
     * Initialisiert ein neues Einzelspieler-Spiel.
     */
    fun startNewGame() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _gameState.update { GameState(isLoading = true) }

            currentTimerDuration = settingsManager.timerDuration.first().toLong()

            val allLocations = locationRepository.getLocationsFromFile("locations.txt")
            val locationsToPlay = locationRepository.getRandomLocations(allLocations, TOTAL_ROUNDS_STANDARD)

            if (locationsToPlay.isEmpty()) {
                Log.e(TAG, "No locations found in 'locations.txt'. Aborting game start.")
                _gameState.update { it.copy(isLoading = false) }
                return@launch
            }

            val gameRounds = locationsToPlay.map { GameRound(targetLocation = it) }

            // Stellt sicher, dass der Ladebildschirm für eine Mindestdauer sichtbar ist.
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingTime = MIN_LOADING_TIME_MS - elapsedTime
            if (remainingTime > 0) {
                delay(remainingTime)
            }

            _gameState.update {
                it.copy(
                    isLoading = false,
                    gameRounds = gameRounds,
                    currentRoundIndex = 0,
                    isGameFinished = false
                )
            }
            startTimer()
        }
    }

    /**
     * Verarbeitet die Schätzung des Spielers.
     */
    fun submitGuess(guess: LatLng, distance: Double, score: Int) {
        pauseTimer()
        val timeTaken = ((currentTimerDuration - remainingTimeInMillis) / 1000).toInt()
        val currentRoundIndex = _gameState.value.currentRoundIndex
        val updatedRounds = _gameState.value.gameRounds.toMutableList()
        val currentRound = updatedRounds[currentRoundIndex]

        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val actualInfo = GeoUtils.getInfoFromLatLng(context, currentRound.targetLocation)
            val guessInfo = GeoUtils.getInfoFromLatLng(context, guess)

            currentRound.guessLocation = guess
            currentRound.result = RoundResult(
                distanceInMeters = distance,
                shameScore = score,
                timeTakenSeconds = timeTaken,
                actualLocation = currentRound.targetLocation,
                guessLocation = guess,
                actualLocationInfo = actualInfo,
                guessLocationInfo = guessInfo
            )
            _gameState.update { it.copy(gameRounds = updatedRounds, forceGuess = false) }
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
    private fun startTimer(durationMillis: Long = currentTimerDuration) {
        timerJob?.cancel()
        remainingTimeInMillis = durationMillis
        timerJob = viewModelScope.launch {
            while (remainingTimeInMillis > 0) {
                _gameState.update { it.copy(formattedTime = formatMillis(remainingTimeInMillis)) }
                delay(1000L)
                remainingTimeInMillis -= 1000L
            }
            _gameState.update { it.copy(formattedTime = "00:00", isMovementAllowed = false, forceGuess = true) }
        }
    }

    /** Hält den Timer an. */
    fun pauseTimer() { timerJob?.cancel() }

    /** Setzt den Timer mit der verbleibenden Zeit fort. */
    fun resumeTimer() { if (remainingTimeInMillis > 0 && !_gameState.value.forceGuess) startTimer(remainingTimeInMillis) }

    /** Formatiert Millisekunden in einen "mm:ss"-String. */
    private fun formatMillis(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /** Wird aufgerufen, wenn der Benutzer versucht, das Spiel zu verlassen. */
    fun onExitAttempt() { pauseTimer(); _gameState.update { it.copy(showExitDialog = true) } }

    /** Wird aufgerufen, wenn der Benutzer den "Verlassen"-Dialog abbricht. */
    fun onExitDismissed() { resumeTimer(); _gameState.update { it.copy(showExitDialog = false) } }

    /** Wird aufgerufen, wenn der Benutzer das Verlassen bestätigt. */
    fun onExitConfirmed() { timerJob?.cancel(); _gameState.update { it.copy(showExitDialog = false) } }
}