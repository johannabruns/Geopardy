package com.example.unmapped.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

/**
 * Eine Datenklasse zur Speicherung von grundlegenden geografischen Informationen zu einem Ort.
 *
 * @property countryCode Der ISO 3166-1 alpha-2 Ländercode (z.B. "DE" für Deutschland).
 * @property continent Der Name des Kontinents (z.B. "EU" für Europa).
 */
data class LocationInfo(
    val countryCode: String?,
    val continent: String?
)

/**
 * Ein Singleton-Objekt mit Hilfsfunktionen für geografische Operationen.
 * Hauptsächlich zur Umwandlung von Koordinaten in Länder- und Kontinentinformationen.
 */
object GeoUtils {

    /**
     * Eine private Map, die Kontinent-Kürzel auf eine Liste von ISO-Ländercodes abbildet.
     * Dies ermöglicht eine schnelle und offline-fähige Bestimmung des Kontinents.
     */
    private val continentMap = mapOf(
        "AF" to listOf("AO", "BF", "BI", /* ... weitere Länder ... */),
        "AS" to listOf("AE", "AF", "AM", /* ... weitere Länder ... */),
        "EU" to listOf("AD", "AL", "AT", /* ... weitere Länder ... */),
        "NA" to listOf("AG", "AI", "AW", /* ... weitere Länder ... */),
        "SA" to listOf("AR", "BO", "BR", /* ... weitere Länder ... */),
        "OC" to listOf("AS", "AU", "CK", /* ... weitere Länder ... */)
    )

    /**
     * Ein Set von Ländercodes, die als "berühmt" gelten,
     * relevant für die Freischaltung des "Cultural Menace"-Badges.
     */
    val famousCountries = setOf("US", "CN", "IN", "JP", "DE")

    /**
     * Ermittelt [LocationInfo] (Ländercode, Kontinent) für gegebene Koordinaten.
     * Nutzt den Android [Geocoder] für die Rückwärts-Geokodierung.
     *
     * @param context Der Anwendungskontext.
     * @param latLng Die Koordinaten, die analysiert werden sollen.
     * @return Ein [LocationInfo]-Objekt. Die Felder können null sein, wenn die Information nicht ermittelt werden konnte.
     */
    fun getInfoFromLatLng(context: Context, latLng: LatLng): LocationInfo {
        // Initialisiert den Geocoder mit der Standard-Sprache des Geräts.
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            // Führt die Rückwärts-Geokodierung durch (max. 1 Ergebnis).
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val countryCode = addresses[0].countryCode // z.B. "DE"
                val continent = getContinentFromCountryCode(countryCode)
                return LocationInfo(countryCode, continent)
            }
        } catch (e: Exception) {
            // Fängt Fehler ab, die bei Netzwerkproblemen oder ungültigen Koordinaten auftreten können.
            Log.e("GeoUtils", "Geocoder failed for $latLng", e)
        }
        // Gibt ein leeres Info-Objekt zurück, wenn die Ermittlung fehlschlägt.
        return LocationInfo(null, null)
    }

    /**
     * Findet das passende Kontinent-Kürzel für einen gegebenen Ländercode.
     *
     * @param countryCode Der ISO-Ländercode.
     * @return Das Kontinent-Kürzel (z.B. "EU") oder null, wenn der Ländercode unbekannt ist.
     */
    private fun getContinentFromCountryCode(countryCode: String?): String? {
        if (countryCode == null) return null
        // Durchsucht die continentMap und gibt den Schlüssel (Kontinent) zurück, dessen Werteliste den Ländercode enthält.
        return continentMap.entries.find { it.value.contains(countryCode) }?.key
    }
}