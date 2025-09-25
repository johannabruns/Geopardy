package com.example.unmapped.data

import android.content.Context
import com.google.android.gms.maps.model.LatLng

/**
 * Verwaltet das Laden von Orten aus den App-Assets und verfolgt, welche Orte bereits gespielt wurden.
 *
 * @param context Der Anwendungskontext.
 */
class LocationRepository(private val context: Context) {
    private val appContext = context.applicationContext

    // Ein einfacher Cache, um das wiederholte Einlesen von Dateien zu vermeiden.
    private val locationCache = mutableMapOf<String, List<LatLng>>()

    // SharedPreferences zur Speicherung der IDs von bereits erfolgreich geratenen Orten.
    private val playedLocationsPrefs = appContext.getSharedPreferences("played_locations_prefs", Context.MODE_PRIVATE)

    /**
     * Lädt eine Liste von Koordinaten aus einer Textdatei im Assets-Ordner.
     * Jede Zeile der Datei sollte das Format "latitude,longitude" haben.
     *
     * @param fileName Der Name der Datei im Assets-Ordner.
     * @return Eine Liste von [LatLng]-Objekten.
     */
    fun getLocationsFromFile(fileName: String): List<LatLng> {
        // Zuerst im Cache prüfen, um die Datei nicht erneut lesen zu müssen.
        if (locationCache.containsKey(fileName)) {
            return locationCache[fileName]!!
        }

        return try {
            val locations = appContext.assets.open(fileName).bufferedReader().useLines { lines ->
                lines
                    .map { it.split(',') } // Jede Zeile am Komma trennen
                    .filter { it.size == 2 } // Nur Zeilen mit genau zwei Teilen (Lat, Lng) verwenden
                    .mapNotNull {
                        try {
                            LatLng(it[0].toDouble(), it[1].toDouble())
                        } catch (e: NumberFormatException) {
                            null // Zeilen mit ungültigen Zahlen ignorieren
                        }
                    }
                    .toList()
            }
            // Die geladene Liste für zukünftige Anfragen im Cache speichern.
            locationCache[fileName] = locations
            locations
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Im Fehlerfall eine leere Liste zurückgeben.
        }
    }

    /**
     * Wählt eine zufällige Anzahl von Orten aus einer gegebenen Liste aus.
     *
     * @param locations Die Quellliste der Orte.
     * @param count Die Anzahl der zufällig auszuwählenden Orte.
     * @return Eine neue Liste mit der gewünschten Anzahl an zufälligen Orten.
     */
    fun getRandomLocations(locations: List<LatLng>, count: Int): List<LatLng> {
        return locations.shuffled().take(count)
    }

    /**
     * Markiert eine Liste von Orten für eine bestimmte Karte als "gespielt".
     *
     * @param mapId Die ID der Karte, zu der die Orte gehören.
     * @param locations Die Liste der Orte, die als gespielt markiert werden sollen.
     */
    fun markLocationsAsPlayed(mapId: String, locations: List<LatLng>) {
        with(playedLocationsPrefs.edit()) {
            val playedSet = getPlayedLocationIds(mapId).toMutableSet()
            locations.forEach { latLng ->
                // Jeder Ort wird als "latitude,longitude"-String gespeichert.
                playedSet.add("${latLng.latitude},${latLng.longitude}")
            }
            putStringSet(mapId, playedSet)
            apply()
        }
    }

    /**
     * Ruft die Set von "gespielten" Orts-IDs für eine bestimmte Karte ab.
     *
     * @param mapId Die ID der Karte.
     * @return Ein Set von Strings, die die gespielten Orte repräsentieren.
     */
    private fun getPlayedLocationIds(mapId: String): Set<String> {
        return playedLocationsPrefs.getStringSet(mapId, emptySet()) ?: emptySet()
    }

    /**
     * Filtert eine Liste von Orten und gibt nur diejenigen zurück, die noch nicht gespielt wurden.
     *
     * @param mapId Die ID der Karte zur Überprüfung.
     * @param allLocations Die vollständige Liste der Orte für diese Karte.
     * @return Eine gefilterte Liste, die nur ungespielte Orte enthält.
     */
    fun filterUnplayedLocations(mapId: String, allLocations: List<LatLng>): List<LatLng> {
        val playedIds = getPlayedLocationIds(mapId)
        if (playedIds.isEmpty()) {
            return allLocations // Wenn noch nichts gespielt wurde, die komplette Liste zurückgeben.
        }
        return allLocations.filter { latLng ->
            "${latLng.latitude},${latLng.longitude}" !in playedIds
        }
    }
}