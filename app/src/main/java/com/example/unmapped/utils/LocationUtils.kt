package com.example.unmapped.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * Ein Singleton-Objekt, das eine Hilfsfunktion zur Distanzberechnung bereitstellt.
 */
object LocationUtils {
    /**
     * Berechnet die kürzeste Distanz (Großkreisentfernung) zwischen zwei geografischen Punkten.
     * Verwendet die hochpräzise und optimierte native Android-Methode.
     *
     * @param start Der Startpunkt als [LatLng].
     * @param end Der Endpunkt als [LatLng].
     * @return Die Distanz in Metern als [Double].
     */
    fun calculateDistanceInMeters(start: LatLng, end: LatLng): Double {
        // Die Ergebnisse der Berechnung werden in dieses Array geschrieben.
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        // Das erste Element des Arrays enthält die Distanz in Metern.
        return results[0].toDouble()
    }
}