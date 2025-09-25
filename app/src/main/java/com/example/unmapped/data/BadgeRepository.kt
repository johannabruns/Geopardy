package com.example.unmapped.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.android.gms.maps.model.LatLng

/**
 * Verwaltet die Speicherung und den Abruf von Badge-Fortschritten.
 * Nutzt SharedPreferences zur Speicherung der Daten.
 *
 * @param context Der Anwendungskontext.
 */
class BadgeRepository(context: Context) {

    // SharedPreferences für den allgemeinen Badge-Fortschritt.
    private val sharedPreferences = context.getSharedPreferences("badge_progress_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val progressListKey = "badge_progress_list"

    // Separate SharedPreferences für den Fortschritt der empfohlenen Karten ("Mastery").
    private val mapPrefs = context.getSharedPreferences("map_progress_prefs", Context.MODE_PRIVATE)

    /**
     * Fügt einen korrekt geratenen Ort zur Fortschrittsliste einer empfohlenen Karte hinzu.
     *
     * @param mapId Die ID der Karte.
     * @param location Die Koordinate des Ortes.
     */
    fun addCorrectlyGuessedLocation(mapId: String, location: LatLng) {
        val key = "map_${mapId}_correct"
        val currentSet = getCorrectlyGuessedLocations(mapId).toMutableSet()
        currentSet.add("${location.latitude},${location.longitude}")
        mapPrefs.edit().putStringSet(key, currentSet).apply()
    }

    /**
     * Ruft die Menge der korrekt geratenen Orte für eine bestimmte empfohlene Karte ab.
     *
     * @param mapId Die ID der Karte.
     * @return Ein Set von Strings, die die Orte repräsentieren.
     */
    fun getCorrectlyGuessedLocations(mapId: String): Set<String> {
        val key = "map_${mapId}_correct"
        return mapPrefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    /**
     * Lädt den Fortschritt aller Badges aus dem Speicher.
     *
     * @return Eine Liste von [BadgeProgress]-Objekten.
     */
    fun getBadgeProgress(): List<BadgeProgress> {
        val json = sharedPreferences.getString(progressListKey, null)
        return if (json != null) {
            // Falls bereits Fortschritt gespeichert ist, wird dieser aus dem JSON geladen.
            val type = object : TypeToken<List<BadgeProgress>>() {}.type
            gson.fromJson(json, type)
        } else {
            // Andernfalls wird eine neue Liste mit Startfortschritt (0) für alle Badges erstellt.
            allBadges.map { BadgeProgress(badgeId = it.id, currentProgress = 0) }
        }
    }

    /**
     * Speichert die komplette Liste des Badge-Fortschritts in den SharedPreferences.
     *
     * @param progressList Die zu speichernde Liste von [BadgeProgress]-Objekten.
     */
    fun saveBadgeProgress(progressList: List<BadgeProgress>) {
        val json = gson.toJson(progressList)
        sharedPreferences.edit().putString(progressListKey, json).apply()
    }

    /**
     * Kombiniert die statischen Badge-Definitionen mit dem dynamischen Fortschritt.
     * Diese Funktion ist besonders nützlich für die Anzeige in der Benutzeroberfläche.
     *
     * @return Eine Map, die jede [BadgeDefinition] dem zugehörigen [BadgeProgress] zuordnet.
     */
    fun getAllBadgesWithProgress(): Map<BadgeDefinition, BadgeProgress> {
        val progressList = getBadgeProgress()
        return allBadges.associateWith { definition ->
            progressList.find { progress -> progress.badgeId == definition.id }
                ?: BadgeProgress(definition.id, 0) // Fallback, falls ein neuer Badge noch nicht im Speicher ist.
        }
    }
}