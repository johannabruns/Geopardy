package com.example.unmapped.data

import android.content.Context
import com.google.android.gms.maps.model.LatLng

/**
 * Ein Repository, das speziell für das Laden von Orten für den "Challenge Mode" zuständig ist.
 * Die Orte werden aus einer festen Datei im Assets-Ordner geladen.
 *
 * @param context Der Anwendungskontext.
 */
class ChallengeLocationRepository(context: Context) {
    private val appContext = context.applicationContext
    private var allLocations: List<LatLng> = emptyList()

    init {
        // Die Orte werden einmalig beim Erstellen des Repositorys geladen.
        try {
            appContext.assets.open("challengelocations.txt").bufferedReader().useLines { lines ->
                allLocations = lines
                    .map { it.split(',') }
                    .filter { it.size == 2 }
                    .mapNotNull {
                        try {
                            LatLng(it[0].toDouble(), it[1].toDouble())
                        } catch (e: NumberFormatException) {
                            null // Ignoriert fehlerhafte Zeilen.
                        }
                    }
                    .toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Im Fehlerfall bleibt die Liste leer.
        }
    }

    /**
     * Wählt eine zufällige Anzahl von Orten aus der geladenen Liste aus.
     *
     * @param count Die Anzahl der zufällig auszuwählenden Orte.
     * @return Eine neue Liste mit den zufälligen Orten.
     */
    fun getRandomLocations(count: Int): List<LatLng> {
        return allLocations.shuffled().take(count)
    }
}