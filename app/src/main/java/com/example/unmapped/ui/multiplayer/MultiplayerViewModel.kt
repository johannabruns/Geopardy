package com.example.unmapped.ui.multiplayer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.*
import com.example.unmapped.utils.GeoUtils
import com.example.unmapped.utils.LocationUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MultiplayerViewModel(
    application: Application, private val playerRepository: PlayerRepository
) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application)
    private val _gameState = MutableStateFlow(MultiplayerGameState())
    val gameState = _gameState.asStateFlow()

    private var timerJob: Job? = null
    private var remainingTimeInMillis: Long = 0

    companion object {
        private const val TOTAL_ROUNDS = 3
        private const val DEFAULT_DURATION_MILLIS = 120_000L
        private const val TAG = "MultiplayerViewModel"
    }

    /**
     * Richtet das Spiel mit den ausgewählten Spielern ein und startet es.
     * @param players Die Liste der teilnehmenden Spieler.
     */
    fun setupGame(players: List<Player>) {
        _gameState.update { it.copy(players = players, phase = GamePhase.LOADING) }
        startNewGame()
    }

    /**
     * Initialisiert ein neues Spiel, indem es Orte lädt und den Zustand auf die Startphase setzt.
     */
    private fun startNewGame() {
        viewModelScope.launch {
            val locations = locationRepository.getLocationsFromFile("locations.txt")
            val gameLocations = locationRepository.getRandomLocations(locations, TOTAL_ROUNDS)
            val gameRounds = gameLocations.map { MultiplayerGameRound(targetLocation = it) }
            _gameState.update {
                it.copy(
                    isLoading = false,
                    gameRounds = gameRounds,
                    currentRoundIndex = 0,
                    currentPlayerIndex = 0,
                    isGameFinished = false,
                    phase = GamePhase.GAME_START // Das Spiel beginnt mit der "Wer ist dran?"-Anzeige.
                )
            }
        }
    }

    /**
     * Verarbeitet die Schätzung eines Spielers, berechnet das Ergebnis und wechselt in die nächste Phase.
     * @param guess Die Koordinaten der abgegebenen Schätzung.
     */
    fun submitGuess(guess: LatLng) {
        pauseTimer()
        val timeTaken = ((DEFAULT_DURATION_MILLIS - remainingTimeInMillis) / 1000).toInt()
        val currentState = _gameState.value
        val currentPlayer = currentState.players[currentState.currentPlayerIndex]
        val currentRound = currentState.gameRounds[currentState.currentRoundIndex]
        val distance = LocationUtils.calculateDistanceInMeters(currentRound.targetLocation, guess)
        val score = calculateShameScore(distance)

        val context = getApplication<Application>().applicationContext
        val actualInfo = GeoUtils.getInfoFromLatLng(context, currentRound.targetLocation)
        val guessInfo = GeoUtils.getInfoFromLatLng(context, guess)

        val result = RoundResult(
            distanceInMeters = distance, shameScore = score, timeTakenSeconds = timeTaken,
            actualLocation = currentRound.targetLocation, guessLocation = guess,
            actualLocationInfo = actualInfo, guessLocationInfo = guessInfo
        )

        // Fügt das Ergebnis des Spielers zur Ergebnis-Map der aktuellen Runde hinzu.
        val updatedResults = currentRound.results.toMutableMap().apply { this[currentPlayer.id] = result }
        val updatedRounds = currentState.gameRounds.toMutableList().apply { this[currentState.currentRoundIndex] = currentRound.copy(results = updatedResults) }
        _gameState.update { it.copy(gameRounds = updatedRounds, forceGuess = false) }

        // Entscheidet, ob der nächste Spieler dran ist oder die Runde zu Ende ist.
        if (currentState.currentPlayerIndex >= currentState.players.size - 1) {
            _gameState.update { it.copy(phase = GamePhase.ROUND_RESULT) }
        } else {
            _gameState.update { it.copy(phase = GamePhase.ROUND_TRANSITION) }
        }
    }

    /**
     * Wechselt zum nächsten Spieler oder startet die erste Runde.
     */
    fun nextTurn() {
        val currentState = _gameState.value
        val nextPlayerIdx = if (currentState.phase == GamePhase.GAME_START) {
            currentState.currentPlayerIndex
        } else {
            currentState.currentPlayerIndex + 1
        }
        _gameState.update {
            it.copy(
                currentPlayerIndex = nextPlayerIdx,
                phase = GamePhase.PLAYING
            )
        }
        startTimer()
    }

    /**
     * Wechselt zur nächsten Runde oder beendet das Spiel, wenn alle Runden gespielt sind.
     */
    fun nextRound() {
        val currentState = _gameState.value
        if (currentState.currentRoundIndex >= TOTAL_ROUNDS - 1) {
            saveFinalScores()
            _gameState.update { it.copy(phase = GamePhase.GAME_OVER) }
        } else {
            _gameState.update {
                it.copy(
                    currentRoundIndex = currentState.currentRoundIndex + 1,
                    // Setzt den Spieler-Index zurück, damit die nächste Runde mit Spieler 0 beginnt.
                    currentPlayerIndex = -1,
                    phase = GamePhase.ROUND_TRANSITION
                )
            }
        }
    }

    /**
     * Wird aufgerufen, wenn der Benutzer versucht, das Spiel zu verlassen.
     */
    fun onExitAttempt() {
        pauseTimer()
        _gameState.update { it.copy(showExitDialog = true) }
    }

    /**
     * Wird aufgerufen, wenn der Benutzer das Verlassen bestätigt.
     */
    fun onExitConfirmed() {
        _gameState.update { it.copy(showExitDialog = false) }
    }

    /**
     * Wird aufgerufen, wenn der Benutzer den "Verlassen"-Dialog abbricht.
     */
    fun onExitDismissed() {
        startTimer(remainingTimeInMillis)
        _gameState.update { it.copy(showExitDialog = false) }
    }

    /**
     * Berechnet die Gesamtpunktzahl für jeden Spieler am Ende des Spiels und speichert sie.
     */
    private fun saveFinalScores() {
        val finalState = _gameState.value
        finalState.players.forEach { player ->
            val totalScore = finalState.gameRounds.sumOf { round ->
                round.results[player.id]?.shameScore ?: 0
            }
            playerRepository.addScoreToPlayer(player.id, totalScore)
        }
    }

    /**
     * Startet den Countdown-Timer.
     * @param durationMillis Die Dauer in Millisekunden.
     */
    fun startTimer(durationMillis: Long = DEFAULT_DURATION_MILLIS) {
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

    /**
     * Hält den Timer an.
     */
    fun pauseTimer() {
        timerJob?.cancel()
    }

    /**
     * Formatiert Millisekunden in einen "mm:ss"-String.
     */
    private fun formatMillis(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}