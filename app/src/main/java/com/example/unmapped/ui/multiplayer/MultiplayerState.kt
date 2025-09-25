package com.example.unmapped.ui.multiplayer

import com.example.unmapped.data.Player
import com.example.unmapped.data.RoundResult
import com.google.android.gms.maps.model.LatLng

/**
 * Repräsentiert den gesamten Zustand eines Multiplayer-Spiels.
 *
 * @property isLoading True, während das Spiel initialisiert wird.
 * @property players Eine Liste der teilnehmenden Spieler.
 * @property gameRounds Eine Liste aller Spielrunden.
 * @property currentRoundIndex Der Index der aktuellen Runde.
 * @property currentPlayerIndex Der Index des Spielers, der aktuell an der Reihe ist.
 * @property phase Die aktuelle Phase des Spiels, die steuert, welcher Bildschirm angezeigt wird.
 * @property isGameFinished True, wenn das Spiel komplett beendet ist.
 * @property isMovementAllowed Gibt an, ob die Navigation in Street View erlaubt ist.
 * @property formattedTime Die verbleibende Zeit für den aktuellen Zug als String.
 * @property forceGuess True, wenn der Timer abgelaufen ist und eine Schätzung erzwungen wird.
 * @property showExitDialog True, wenn der Dialog zum Verlassen des Spiels angezeigt wird.
 */
data class MultiplayerGameState(
    val isLoading: Boolean = true,
    val players: List<Player> = emptyList(),
    val gameRounds: List<MultiplayerGameRound> = emptyList(),
    val currentRoundIndex: Int = 0,
    val currentPlayerIndex: Int = 0,
    val phase: GamePhase = GamePhase.LOADING,
    val isGameFinished: Boolean = false,
    val isMovementAllowed: Boolean = true,
    val formattedTime: String = "02:00",
    val forceGuess: Boolean = false,
    val showExitDialog: Boolean = false
)

/**
 * Repräsentiert eine einzelne Runde in einem Multiplayer-Spiel.
 *
 * @property targetLocation Der zu erratende Ort für diese Runde.
 * @property results Eine Map, die Spieler-IDs auf ihre jeweiligen [RoundResult] für diese Runde abbildet.
 */
data class MultiplayerGameRound(
    val targetLocation: LatLng,
    val results: Map<String, RoundResult> = emptyMap()
)

/**
 * Definiert die verschiedenen Phasen eines Multiplayer-Spiels.
 * Dient als Zustandsmaschine, um den UI-Fluss zu steuern.
 */
enum class GamePhase {
    LOADING,           // Das Spiel wird im Hintergrund geladen.
    GAME_START,        // Zeigt an, welcher Spieler beginnt.
    PLAYING,           // Ein Spieler ist aktiv in der Street View Ansicht.
    ROUND_TRANSITION,  // Zeigt zwischen den Zügen an, wer als Nächstes dran ist.
    ROUND_RESULT,      // Zeigt die Ergebnisse aller Spieler für eine abgeschlossene Runde an.
    GAME_OVER          // Zeigt den finalen Endbildschirm mit den Gesamtergebnissen an.
}