package com.example.unmapped.data

import android.content.Context
import android.util.Log
import com.example.unmapped.utils.GeoUtils
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

/**
 * Ein Singleton-Objekt, das die Logik zur Verarbeitung von Spielergebnissen
 * zur Aktualisierung von Badges und globalen Statistiken kapselt.
 */
object BadgeManager {

    // Konstanten für Metadaten-Schlüssel, um Tippfehler zu vermeiden.
    private const val STREAK_COUNTER = "streak_counter"
    private const val CONTINENTS_MISSED = "continents_missed"

    // Schwellenwert in Metern, unter dem ein Ort als "gemeistert" gilt.
    private const val MASTERY_THRESHOLD_METERS = 500.0

    /**
     * Verarbeitet das Ergebnis einer Runde aus einer empfohlenen Karte, um "Mastery"-Badges freizuschalten.
     *
     * @param context Der Anwendungskontext.
     * @param roundResult Das Ergebnis der Runde.
     * @param mapId Die ID der gespielten empfohlenen Karte.
     */
    fun processRecommendedMapResult(context: Context, roundResult: RoundResult, mapId: String) {
        // Nur wenn die Schätzung sehr genau war, wird der Ort als "korrekt" gewertet.
        if (roundResult.distanceInMeters <= MASTERY_THRESHOLD_METERS) {
            val badgeRepository = BadgeRepository(context)

            // Markiert diesen Ort als korrekt geraten für die gegebene Karte.
            badgeRepository.addCorrectlyGuessedLocation(mapId, roundResult.actualLocation)

            val totalLocationsInMap = getLocationsForMap(mapId).size
            val correctlyGuessedCount = badgeRepository.getCorrectlyGuessedLocations(mapId).size

            // Wenn alle Orte einer Karte gemeistert wurden, wird der entsprechende Badge freigeschaltet.
            if (totalLocationsInMap > 0 && correctlyGuessedCount >= totalLocationsInMap) {
                val badgeIdToUnlock = mapId.uppercase() + "_MASTER"
                val progressMap = badgeRepository.getBadgeProgress().associateBy { it.badgeId }.toMutableMap()
                progressMap[badgeIdToUnlock]?.currentProgress = 1 // Setzt Fortschritt auf 1 (freigeschaltet).
                badgeRepository.saveBadgeProgress(progressMap.values.toList())
                Log.d("BadgeManager", "Unlocked MASTER badge: $badgeIdToUnlock")
            }
        }
    }
    /**
     * Verarbeitet eine Liste von Runden-Ergebnissen, um Badges und Statistiken zu aktualisieren.
     *
     * @param context Der Anwendungskontext.
     * @param gameRounds Eine Liste der Ergebnisse der gespielten Runden.
     */
    fun processGameResults(context: Context, gameRounds: List<RoundResult>) {
        if (gameRounds.isEmpty()) return

        val badgeRepository = BadgeRepository(context)
        val statsRepository = StatsRepository(context)
        // Lädt den aktuellen Fortschritt aller Badges als Map für schnellen Zugriff.
        val progressMap = badgeRepository.getBadgeProgress().associateBy { it.badgeId }.toMutableMap()

        gameRounds.forEach { result ->
            // Prüft für jede Runde alle relevanten Badges.
            checkAllBadgesForRound(result, progressMap)
            // Aktualisiert die globalen Statistiken mit dem Ergebnis dieser Runde.
            statsRepository.updateStats(result)
        }

        // Speichert den gesamten aktualisierten Badge-Fortschritt.
        badgeRepository.saveBadgeProgress(progressMap.values.toList())
        Log.d("BadgeManager", "Badge and Stats progress updated.")
    }

    /**
     * Überprüft das Ergebnis einer einzelnen Runde gegen die Kriterien aller Badges.
     *
     * @param result Das Ergebnis der zu prüfenden Runde.
     * @param progressMap Die Map mit dem aktuellen Badge-Fortschritt, die hier modifiziert wird.
     */
    private fun checkAllBadgesForRound(
        result: RoundResult,
        progressMap: MutableMap<String, BadgeProgress>
    ) {
        // Zeit-basierte Badges
        if (result.timeTakenSeconds <= 30) progressMap.increment("SMART_ASS")
        if (result.timeTakenSeconds >= 119) progressMap.increment("CRITICAL_OVERTHINKER")

        // Distanz-basierte Zähler-Badges
        if (result.distanceInMeters in 2000.0..25000.0) progressMap.increment("LOST_TOURIST")
        if (result.distanceInMeters in 25000.0..250000.0) progressMap.increment("BARE_MINIMUM")
        if (result.distanceInMeters in 1_000_000.0..5_000_000.0) progressMap.increment("COLUMBUS")
        if (result.distanceInMeters > 100_000) progressMap.increment("GEOGRAPHY_DROPOUT")

        // Berechnung der reinen Breiten- und Längengrad-Abweichung
        val pointForLatCheck = LatLng(result.guessLocation.latitude, result.actualLocation.longitude)
        val latDistance = SphericalUtil.computeDistanceBetween(result.actualLocation, pointForLatCheck)
        val pointForLngCheck = LatLng(result.actualLocation.latitude, result.guessLocation.longitude)
        val lngDistance = SphericalUtil.computeDistanceBetween(result.actualLocation, pointForLngCheck)

        if (latDistance > 200_000) progressMap.increment("LATITUDE_LOSER")
        if (lngDistance > 200_000) progressMap.increment("LONGITUDE_LOSER")

        // Geografie-basierte Badges
        val actualInfo = result.actualLocationInfo
        val guessInfo = result.guessLocationInfo

        if (actualInfo.continent != null && actualInfo.continent != guessInfo.continent) {
            progressMap.increment("CONTINENTAL_DRIFT")
            progressMap.increment("US_AMERICAN")
        }
        if (actualInfo.countryCode == "DE" && guessInfo.countryCode != "DE") {
            progressMap.increment("NATIONAL_EMBARRASSMENT")
        }
        if (actualInfo.continent != "EU" && guessInfo.continent == "EU") {
            progressMap.increment("EUROCENTRIC_MUCH")
        }
        if (actualInfo.countryCode in GeoUtils.famousCountries && actualInfo.countryCode != guessInfo.countryCode) {
            progressMap.increment("CULTURAL_MENACE")
        }

        // "Global Menace"-Badge: Verfolgt, welche Kontinente falsch geraten wurden, mithilfe einer Bitmaske.
        val globalMenaceProgress = progressMap["GLOBAL_MENACE"]
        if (globalMenaceProgress != null && actualInfo.continent != null && actualInfo.continent != guessInfo.continent) {
            val missedContinentValue = continentToIndex(actualInfo.continent)
            val currentMissedBitmask = globalMenaceProgress.metadata[CONTINENTS_MISSED] ?: 0
            // Fügt den neuen Kontinent zur Bitmaske hinzu.
            val newMissedBitmask = currentMissedBitmask or (1 shl missedContinentValue)
            globalMenaceProgress.metadata[CONTINENTS_MISSED] = newMissedBitmask
            // Der Fortschritt ist die Anzahl der gesetzten Bits in der Maske.
            globalMenaceProgress.currentProgress = Integer.bitCount(newMissedBitmask)
        }

        // "In Folge"-Badges (Streaks)
        updateStreakBadge(result.distanceInMeters in 50_000.0..100_000.0, "CONSISTENTLY_MID", progressMap)
        updateStreakBadge(result.distanceInMeters > 100_000, "CHRONICALLY_WRONG", progressMap)
        val wrongHemisphere = (result.actualLocation.latitude * result.guessLocation.latitude) < 0
        updateStreakBadge(wrongHemisphere, "FLAT_EARTHER", progressMap)
    }

    /**
     * Ordnet einem Kontinent-Code einen Index für die Bitmaske zu.
     */
    private fun continentToIndex(continent: String?): Int {
        return when(continent) {
            "EU" -> 0
            "AS" -> 1
            "AF" -> 2
            "NA" -> 3
            "SA" -> 4
            "OC" -> 5
            else -> 0 // Fallback
        }
    }

    /**
     * Eine Hilfsfunktion (Extension), um den Fortschritt eines Badges sicher zu erhöhen.
     * Der Fortschritt wird nur erhöht, wenn das Ziel noch nicht erreicht ist.
     */
    private fun MutableMap<String, BadgeProgress>.increment(badgeId: String, amount: Int = 1) {
        this[badgeId]?.let {
            val required = allBadges.find { b -> b.id == badgeId }?.requiredProgress ?: 1
            if (it.currentProgress < required) {
                it.currentProgress += amount
            }
        }
    }

    /**
     * Aktualisiert den Fortschritt für einen Badge, der eine Serie ("Streak") erfordert.
     *
     * @param conditionMet Gibt an, ob die Bedingung für die Serie in dieser Runde erfüllt wurde.
     * @param badgeId Die ID des Streak-Badges.
     * @param progressMap Die Map mit dem aktuellen Badge-Fortschritt.
     */
    private fun updateStreakBadge(
        conditionMet: Boolean,
        badgeId: String,
        progressMap: MutableMap<String, BadgeProgress>
    ) {
        val progress = progressMap[badgeId] ?: return
        var currentStreak = progress.metadata[STREAK_COUNTER] ?: 0

        if (conditionMet) {
            currentStreak++
        } else {
            currentStreak = 0 // Serie unterbrochen, Zähler zurücksetzen.
        }

        progress.metadata[STREAK_COUNTER] = currentStreak
        // Der sichtbare Fortschritt ist immer der höchste erreichte Streak.
        if (currentStreak > progress.currentProgress) {
            progress.currentProgress = currentStreak
        }
    }
}