package com.example.unmapped.ui.challenge

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.ChallengeLocationRepository
import com.example.unmapped.data.RoundResult
import com.example.unmapped.data.SettingsManager
import com.example.unmapped.utils.GeoUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repräsentiert den gesamten Zustand eines Challenge-Spiels.
 *
 * @property isLoading Gibt an, ob das Spiel gerade initialisiert wird.
 * @property gameRounds Eine Liste aller Runden des Spiels.
 * @property currentRoundIndex Der Index der aktuell gespielten Runde.
 * @property isGameFinished True, wenn alle Runden abgeschlossen sind.
 * @property formattedTime Die verbleibende Zeit als formatierter String (z.B. "01:59").
 * @property showExitDialog True, wenn der Dialog zum Bestätigen des Spielabbruchs angezeigt werden soll.
 * @property isMovementAllowed Gibt an, ob die Navigation in Street View erlaubt ist.
 * @property forceGuess True, wenn der Timer abgelaufen ist und der Spieler eine Schätzung abgeben muss.
 */
data class ChallengeGameState(
    val isLoading: Boolean = true,
    val gameRounds: List<ChallengeGameRound> = emptyList(),
    val currentRoundIndex: Int = 0,
    val isGameFinished: Boolean = false,
    val formattedTime: String = "02:00",
    val showExitDialog: Boolean = false,
    val isMovementAllowed: Boolean = true,
    val forceGuess: Boolean = false
)

/**
 * Repräsentiert eine einzelne Runde im Challenge-Modus.
 *
 * @property targetLocation Die tatsächliche Koordinate, die erraten werden muss.
 * @property guessLocation Die vom Spieler abgegebene Schätzung.
 * @property result Das Ergebnis der Runde, nachdem eine Schätzung abgegeben wurde.
 */
data class ChallengeGameRound(
    val targetLocation: LatLng,
    var guessLocation: LatLng? = null,
    var result: RoundResult? = null
)

class ChallengeViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = ChallengeLocationRepository(application)
    private val settingsManager = SettingsManager(application)
    private val _gameState = MutableStateFlow(ChallengeGameState())
    val gameState = _gameState.asStateFlow()

    // Job-Instanz zur Steuerung des Countdown-Timers.
    private var timerJob: Job? = null
    // Speichert die verbleibende Zeit, um den Timer anhalten und fortsetzen zu können.
    private var remainingTimeInMillis: Long = 0
    // Speichert die für dieses Spiel gültige Timer-Dauer aus den Einstellungen.
    private var currentTimerDuration: Long = SettingsManager.DEFAULT_TIMER_DURATION.toLong()

    companion object {
        private const val TOTAL_ROUNDS = 5
        private const val TAG = "ChallengeViewModel"
    }

    /**
     * Initialisiert ein neues Challenge-Spiel.
     */
    fun startNewGame() {
        viewModelScope.launch {
            Log.d(TAG, "Starting new challenge game...")
            _gameState.update { ChallengeGameState(isLoading = true) }

            // Liest die vom Benutzer eingestellte Timer-Dauer aus, bevor das Spiel startet.
            currentTimerDuration = settingsManager.timerDuration.first().toLong()

            // Lädt die Orte asynchron im IO-Kontext.
            val locations = withContext(Dispatchers.IO) {
                locationRepository.getRandomLocations(TOTAL_ROUNDS)
            }
            val gameRounds = locations.map { ChallengeGameRound(targetLocation = it) }

            // Aktualisiert den Zustand mit den neuen Spieldaten.
            _gameState.update {
                it.copy(
                    isLoading = false,
                    gameRounds = gameRounds,
                    currentRoundIndex = 0,
                    isGameFinished = false
                )
            }
            Log.d(TAG, "Challenge game initialized with ${gameRounds.size} rounds.")
            startTimer()
        }
    }

    /**
     * Verarbeitet die Schätzung des Spielers.
     *
     * @param guess Die Koordinaten der Schätzung.
     * @param distance Die berechnete Distanz zum Ziel.
     * @param score Der berechnete Shame Score.
     */
    fun submitGuess(guess: LatLng, distance: Double, score: Int) {
        pauseTimer()
        // Berechnet die benötigte Zeit aus der Differenz der Startdauer und der Restzeit.
        val timeTaken = ((currentTimerDuration - remainingTimeInMillis) / 1000).toInt()
        val currentRoundIndex = _gameState.value.currentRoundIndex
        val updatedRounds = _gameState.value.gameRounds.toMutableList()
        val currentRound = updatedRounds[currentRoundIndex]

        val context = getApplication<Application>().applicationContext
        val actualInfo = GeoUtils.getInfoFromLatLng(context, currentRound.targetLocation)
        val guessInfo = GeoUtils.getInfoFromLatLng(context, guess)

        // Speichert das Ergebnis in der aktuellen Runde.
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

    /**
     * Wechselt zur nächsten Runde oder beendet das Spiel.
     */
    fun nextRound() {
        val nextRoundIndex = _gameState.value.currentRoundIndex + 1
        if (nextRoundIndex < TOTAL_ROUNDS) {
            // Wenn es weitere Runden gibt, wird der Index erhöht und der Timer neu gestartet.
            _gameState.update { it.copy(currentRoundIndex = nextRoundIndex, isMovementAllowed = true, forceGuess = false) }
            Log.d(TAG, "Moving to round $nextRoundIndex")
            startTimer()
        } else {
            // Andernfalls wird das Spiel als beendet markiert.
            _gameState.update { it.copy(isGameFinished = true) }
            Log.d(TAG, "Game finished")
        }
    }

    /**
     * Startet den Countdown-Timer für die aktuelle Runde.
     *
     * @param durationMillis Die Dauer, mit der der Timer starten soll.
     */
    private fun startTimer(durationMillis: Long = currentTimerDuration) {
        timerJob?.cancel() // Stoppt einen eventuell laufenden vorherigen Timer.
        remainingTimeInMillis = durationMillis
        timerJob = viewModelScope.launch {
            Log.d(TAG, "Timer started: ${durationMillis}ms")
            // Die Schleife läuft, solange Zeit übrig ist.
            while (remainingTimeInMillis > 0) {
                _gameState.update { it.copy(formattedTime = formatMillis(remainingTimeInMillis)) }
                delay(1000L) // Wartet eine Sekunde.
                remainingTimeInMillis -= 1000L
            }
            // Wenn die Zeit abgelaufen ist, wird der Spieler gezwungen, zu raten.
            _gameState.update { it.copy(formattedTime = "00:00", isMovementAllowed = false, forceGuess = true) }
            Log.d(TAG, "Timer finished")
        }
    }

    /**
     * Hält den Timer an.
     */
    fun pauseTimer() {
        timerJob?.cancel()
        Log.d(TAG, "Timer paused at ${formatMillis(remainingTimeInMillis)}")
    }

    /**
     * Setzt den Timer mit der verbleibenden Zeit fort.
     */
    fun resumeTimer() {
        if (remainingTimeInMillis > 0) {
            Log.d(TAG, "Timer resumed with ${remainingTimeInMillis}ms remaining")
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
        Log.d(TAG, "Exit attempted")
    }

    /**
     * Wird aufgerufen, wenn der Benutzer den "Verlassen"-Dialog abbricht.
     */
    fun onExitDismissed() {
        resumeTimer()
        _gameState.update { it.copy(showExitDialog = false) }
        Log.d(TAG, "Exit dismissed")
    }

    /**
     * Wird aufgerufen, wenn der Benutzer das Verlassen bestätigt.
     */
    fun onExitConfirmed() {
        timerJob?.cancel()
        _gameState.update { it.copy(showExitDialog = false) }
        Log.d(TAG, "Exit confirmed")
    }
}