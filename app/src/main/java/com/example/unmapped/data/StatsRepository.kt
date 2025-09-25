package com.example.unmapped.data

import android.content.Context
import kotlin.math.max
import kotlin.math.min

/**
 * Eine Datenklasse zur Kapselung aller aggregierten Spielstatistiken.
 *
 * @property totalRounds Gesamtzahl der gespielten Runden.
 * @property totalDistanceKm Gesamtdistanz aller Schätzungen in Kilometern.
 * @property bestDistanceKm Die kürzeste Distanz, die je erreicht wurde (kann null sein).
 * @property worstDistanceKm Die weiteste Distanz, die je erreicht wurde (kann null sein).
 * @property fastGuesses Anzahl der Schätzungen unter einer bestimmten Zeitgrenze (30s).
 * @property slowGuesses Anzahl der Schätzungen über einer bestimmten Zeitgrenze (120s).
 * @property totalShameScore Gesamter Shame Score (wird hier nur zur Anzeige geladen).
 */
data class GameStats(
    val totalRounds: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val bestDistanceKm: Double? = null,
    val worstDistanceKm: Double? = null,
    val fastGuesses: Int = 0,
    val slowGuesses: Int = 0,
    val totalShameScore: Int = 0
) {
    /**
     * Berechnet die durchschnittliche Distanz pro Runde.
     * Gibt 0.0 zurück, wenn noch keine Runden gespielt wurden, um eine Division durch Null zu vermeiden.
     */
    val avgDistanceKm: Double
        get() = if (totalRounds > 0) totalDistanceKm / totalRounds else 0.0
}

/**
 * Verwaltet die Speicherung und den Abruf von Spielstatistiken mithilfe von SharedPreferences.
 *
 * @param context Der Anwendungskontext.
 */
class StatsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("game_stats_prefs", Context.MODE_PRIVATE)

    /**
     * Lädt die aktuellen Statistiken aus den SharedPreferences.
     *
     * @return Ein [GameStats]-Objekt mit den geladenen Werten.
     */
    fun getStats(): GameStats {
        return GameStats(
            totalRounds = prefs.getInt("totalRounds", 0),
            totalDistanceKm = prefs.getFloat("totalDistanceKm", 0f).toDouble(),
            // Prüft, ob der Schlüssel existiert, bevor er gelesen wird, um nullable Werte korrekt zu behandeln.
            bestDistanceKm = if (prefs.contains("bestDistanceKm")) prefs.getFloat("bestDistanceKm", Float.MAX_VALUE).toDouble() else null,
            worstDistanceKm = if (prefs.contains("worstDistanceKm")) prefs.getFloat("worstDistanceKm", 0f).toDouble() else null,
            fastGuesses = prefs.getInt("fastGuesses", 0),
            slowGuesses = prefs.getInt("slowGuesses", 0),
            totalShameScore = prefs.getInt("totalShameScore", 0)
        )
    }

    /**
     * Aktualisiert die Statistiken basierend auf dem Ergebnis einer neuen Runde.
     *
     * @param roundResult Das Ergebnis der gerade beendeten Runde.
     */
    fun updateStats(roundResult: RoundResult) {
        val currentStats = getStats()
        val roundDistanceKm = roundResult.distanceInMeters / 1000.0

        prefs.edit().apply {
            putInt("totalRounds", currentStats.totalRounds + 1)
            putFloat("totalDistanceKm", (currentStats.totalDistanceKm + roundDistanceKm).toFloat())

            // Aktualisiert die beste Distanz, falls die neue Runde besser war.
            val best = currentStats.bestDistanceKm
            putFloat("bestDistanceKm", min(best ?: Double.MAX_VALUE, roundDistanceKm).toFloat())

            // Aktualisiert die schlechteste Distanz, falls die neue Runde schlechter war.
            val worst = currentStats.worstDistanceKm
            putFloat("worstDistanceKm", max(worst ?: 0.0, roundDistanceKm).toFloat())

            // Zählt schnelle und langsame Runden basierend auf Zeit.
            if (roundResult.timeTakenSeconds <= 30) {
                putInt("fastGuesses", currentStats.fastGuesses + 1)
            }
            if (roundResult.timeTakenSeconds >= 120) {
                putInt("slowGuesses", currentStats.slowGuesses + 1)
            }
            apply()
        }
    }
}