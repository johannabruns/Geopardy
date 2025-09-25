package com.example.unmapped.data

import com.example.unmapped.utils.LocationInfo
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

// --- Vordefinierte Orte für die Recommended Maps ---
private val touristTrapsLocations = listOf(
    LatLng(40.758141918218215, -73.98556339059482), // Times Square
    LatLng(48.85837,2.29448), // Eiffelturm
    LatLng(43.72302,10.39663), //Schiefer Turm von Pisa
    LatLng(36.43211,25.42274), //Santorini
    LatLng(34.1016,-118.3267), //Hollywood Walk of Fame
    LatLng(-8.431615675788212,115.27931372545669), //Bali Rice Terrace
    LatLng(41.90094,12.48282), //Fontana di Trevi
    LatLng(41.88263,-87.62347) //Cloud Gate
)

private val popCultureHotspotsLocations = listOf(
    LatLng(51.53208661844163, -0.17733156427290014), // Abbey Road Crossing
    LatLng(51.531662597646516, -0.12359504241888854), // Kings Cross Station
    LatLng(-37.857915721189194, 175.68038076424554), // Hobbit Filming Locations
    LatLng(47.95960899040987, -124.3927660840214), // Forks Welcome Sign
    LatLng(39.174966450733045, 23.651502126166665) // Mamma Mia Church
)

private val cancelledDestinationsLocations = listOf(
    LatLng(28.4112021983323, -81.46125968952879), //SeaWorld Orlando
    LatLng(25.208835973937937, 55.27398067222927), //Dubai
    LatLng(34.044508292092836, -118.25072321819881), //Cecil Hotel, LA
    LatLng(25.289639817407693, 51.53303514499203), //Doha, Katar
    LatLng(55.757845632424775, 37.60879438959731), //Moskau
)

private val conspiracyCoreLocations = listOf(
    LatLng(57.290986309158036, -4.447722401522474), //Loch Ness
    LatLng(39.84638651968429, -104.67407641614274), //Denver International Airport
    LatLng(33.392645753115154, -104.5229393417492), //Roswell, New Mexico
    LatLng(51.17889,-1.82611), //Stone Henge
    LatLng(41.90184100097306, 12.457251348781584), //Vatikan
    LatLng(-27.12502092798182, -109.27716304844438), //Moai
    LatLng(34.101518239914036, -118.32819749158988) //Scientology
)

/**
 * Eine Map, die die empfohlenen Karten-IDs mit den zugehörigen Listen von Orten verknüpft.
 * Dies ermöglicht einen einfachen Zugriff auf die Orte über eine ID.
 */
private val recommendedMaps = mapOf(
    "tourist_traps" to touristTrapsLocations,
    "pop_culture_hotspots" to popCultureHotspotsLocations,
    "cancelled_destinations" to cancelledDestinationsLocations,
    "conspiracy_core" to conspiracyCoreLocations
)

/**
 * Ruft die Liste der Orte für eine bestimmte empfohlene Karte ab.
 *
 * @param mapId Die ID der Karte (z.B. "tourist_traps").
 * @return Eine Liste von [LatLng]-Objekten oder eine leere Liste, wenn die ID ungültig ist.
 */
fun getLocationsForMap(mapId: String): List<LatLng> {
    return recommendedMaps[mapId] ?: emptyList()
}

/**
 * Definiert die verschiedenen "Shame"-Stufen basierend auf der Abweichung der Schätzung.
 * Jede Stufe ist mit einer Reihe von spöttischen Kommentaren ("Roasts") verknüpft.
 */
enum class ShameTier {
    SUSPICIOUSLY_GOOD, BARE_MINIMUM, MEDIOCRE, VAGUELY_IN_THE_AREA,
    WRONG_ZIP_CODE, DRUNK_COMPASS, COLUMBUS, FLAT_EARTHER
}

/**
 * Eine Map, die jede [ShameTier] einer Liste von passenden "Roasts" zuordnet.
 */
val roastsByTier = mapOf(
    ShameTier.SUSPICIOUSLY_GOOD to listOf(
        "That’s… suspiciously good.", "You have to be cheating.", "This smells like Google Maps open on the side.",
        "Impressive… for once.", "Suspiciously smart for you.", "Okay Einstein, calm down.", "Ok, pop off."
    ),
    ShameTier.BARE_MINIMUM to listOf(
        "Have you actually been to school?", "Barely counts as knowing where you are.", "Half a braincell moment.",
        "Congrats, you located the neighborhood.", "Congrats, you unlocked the bare minimum.",
        "That’s giving lucky guess, not brainpower.", "You tried, I guess.", "Not you actually knowing stuff."
    ),
    ShameTier.MEDIOCRE to listOf(
        "Not tragic, but not giving genius either.", "You missed it like your morning alarm.",
        "Close… but not close enough to brag.", "You call that a guess? Cute.", "You aimed for smart and landed on mediocre.",
        "That’s a B- at best.", "Close. Like emotionally, not factually."
    ),
    ShameTier.VAGUELY_IN_THE_AREA to listOf(
        "This ain’t it, chief.", "You’re like… vaguely in the area.",
        "The confidence? Unreal. The accuracy? Not so much.", "You’re giving GPS malfunction.",
        "Does this count as cultural ignorance?", "Girl, be serious."
    ),
    ShameTier.WRONG_ZIP_CODE to listOf(
        "Wow, you know continents exist, I’ll give you that.", "And you said that with confidence and everything.",
        "That’s a long-distance relationship with reality.", "If being wrong burned calories, you’d be shredded.",
        "This is not the serve you thought it was.", "Did you even try?", "This guess should be illegal."
    ),
    ShameTier.DRUNK_COMPASS to listOf(
        "Not you crossing country lines like it’s nothing.", "Your compass is drunk.",
        "Okay, but you were confident too.", "Lowkey impressive how off you are.",
        "That’s a long-distance relationship with the truth.", "Your internal compass is American-coded.",
        "You’re treating borders like suggestions.",
        "This ain’t geography, this is improv."
    ),
    ShameTier.COLUMBUS to listOf(
        "Are you American?", "Okay Columbus, wrong coast.",
        "Does this count as a hate crime?", "Pack it up, Dora the Explorer.", "Your sense of direction is in witness protection.",
        "Not you crossing borders like they’re your territory.", "You’re basically playing blindfolded."
    ),
    ShameTier.FLAT_EARTHER to listOf(
        "Geography is just not your aesthetic.", "Wrong continent, chief.",
        "Pack it up, Marco Polo.", "You’re basically speedrunning colonialism.", "That’s a world tour, not a guess."
    )
)

/**
 * Definiert die statischen Eigenschaften eines Badges.
 *
 * @property id Die einzigartige ID des Badges.
 * @property name Der Anzeigename des Badges.
 * @property description Eine Beschreibung, was der Badge bedeutet.
 * @property requiredProgress Die Anzahl der Aktionen, die zum Freischalten erforderlich sind.
 */
data class BadgeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val requiredProgress: Int = 1
)

/**
 * Speichert den aktuellen Fortschritt für einen bestimmten Badge.
 *
 * @property badgeId Die ID des Badges, zu dem dieser Fortschritt gehört.
 * @property currentProgress Der aktuelle Zählerstand.
 * @property metadata Eine Map für zusätzliche Daten, z.B. für die Verfolgung von Serien ("Streaks").
 */
data class BadgeProgress(
    val badgeId: String,
    var currentProgress: Int = 0,
    val metadata: MutableMap<String, Int> = mutableMapOf()
)

/**
 * Die Master-Liste aller im Spiel verfügbaren Badges.
 */
val allBadges = listOf(
    BadgeDefinition("SMART_ASS", "Smart Ass", "You guessed 5 times in under 30 seconds. Speed isn’t the same as accuracy, you know.", 5),
    BadgeDefinition("CRITICAL_OVERTHINKER", "Critical Overthinker", "You waited for the timer to run out 5 times. Commitment to indecision, impressive.", 5),
    BadgeDefinition("US_AMERICAN", "U.S. American", "Only a true American could land on the wrong continent 3 times. Your geography teacher is crying.", 3),
    BadgeDefinition("FLAT_EARTHER", "Flat Earther", "You picked the wrong hemisphere 3 rounds in a row. Science weeps.", 3),
    BadgeDefinition("LOST_TOURIST", "Lost Tourist", "You were 2–25 km off, 5 times. Close enough to smell it, still too far to matter.", 5),
    BadgeDefinition("NATIONAL_EMBARRASSMENT", "National Embarrassment", "You missed 3 German locations. Even your homeland wants nothing to do with you.", 3),
    BadgeDefinition("EUROCENTRIC_MUCH", "Eurocentric Much?", "You called 3 non-European places ‘Europe.’ Colonizer vibes.", 3),
    BadgeDefinition("CHRONICALLY_WRONG", "Chronically Wrong", "You stayed 100+ km off for 10 rounds in a row. A masterclass in consistent failure.", 10),
    BadgeDefinition("GEOGRAPHY_DROPOUT", "Geography Dropout", "You missed by over 100 km, 10 times. Graduation denied.", 10),
    BadgeDefinition("CULTURAL_MENACE", "Cultural Menace", "You messed up 3 of the 5 most famous countries. The audacity is impressive.", 3),
    BadgeDefinition("COLUMBUS", "Columbus", "You guessed 5 times between 1,000 and 5,000 km off. Boldly wrong, just like the original.", 5),
    BadgeDefinition("GLOBAL_MENACE", "Global Menace", "You got every continent wrong at least once. Congrats on uniting the world - in disappointment.", 6),
    BadgeDefinition("BARE_MINIMUM", "Bare Minimum", "You landed 5 guesses between 25 and 250 km. Congrats on achieving mediocrity.", 5),
    BadgeDefinition("LATITUDE_LOSER", "Latitude Loser", "You missed the latitude by 200+ km, 5 times. North? South? Still wrong.", 5),
    BadgeDefinition("LONGITUDE_LOSER", "Longitude Loser", "You missed the longitude by 200+ km, 5 times. East, west… who cares, right?", 5),
    BadgeDefinition("CONTINENTAL_DRIFT", "Continental Drift", "You guessed the wrong continent 3 times. Even tectonic plates drift with more accuracy.", 3),
    BadgeDefinition("TOURIST_TRAPS_MASTER", "Tourist Traps", "You actually nailed all the tourist traps. Congrats, you fell for all of them.", 1),
    BadgeDefinition("POP_CULTURE_MASTER", "Pop Culture Hotspots", "You got every pop culture location right. TV raised you well.", 1),
    BadgeDefinition("CANCELLED_DESTINATIONS_MASTER", "Cancelled Destinations", "You guessed every cancelled destination. Problematic, but consistent.", 1),
    BadgeDefinition("CONSPIRACY_CORE_MASTER", "Conspiracy Core", "You guessed every conspiracy hotspot. Put the tinfoil hat on already.", 1),
    BadgeDefinition("CONSISTENTLY_MID", "Consistently Mid", "You stayed between 50–100 km off for 5 rounds straight. Commitment to mediocrity.", 5)
)

/**
 * Kapselt alle relevanten Daten und Ergebnisse einer einzelnen Spielrunde.
 *
 * @property distanceInMeters Die Abweichung zwischen Schätzung und Ziel in Metern.
 * @property shameScore Die für diese Runde berechneten "Shame Points".
 * @property timeTakenSeconds Die für die Schätzung benötigte Zeit in Sekunden.
 * @property actualLocation Die tatsächlichen Koordinaten des Ziels.
 * @property guessLocation Die vom Spieler gewählten Koordinaten.
 * @property actualLocationInfo Zusätzliche Geodaten zum Zielort (Land, Kontinent).
 * @property guessLocationInfo Zusätzliche Geodaten zum geratenen Ort.
 */
data class RoundResult(
    val distanceInMeters: Double,
    val shameScore: Int,
    val timeTakenSeconds: Int,
    val actualLocation: LatLng,
    val guessLocation: LatLng,
    val actualLocationInfo: LocationInfo,
    val guessLocationInfo: LocationInfo
)

/**
 * Wählt einen zufälligen "Roast" basierend auf der Distanz aus.
 *
 * @param distanceInMeters Die Distanz der Abweichung.
 * @return Einen zufälligen String aus der passenden [ShameTier].
 */
fun getRandomRoast(distanceInMeters: Double): String {
    val tier = when {
        distanceInMeters <= 500 -> ShameTier.SUSPICIOUSLY_GOOD
        distanceInMeters <= 2000 -> ShameTier.BARE_MINIMUM
        distanceInMeters <= 25000 -> ShameTier.MEDIOCRE
        distanceInMeters <= 250000 -> ShameTier.VAGUELY_IN_THE_AREA
        distanceInMeters <= 500000 -> ShameTier.WRONG_ZIP_CODE
        distanceInMeters <= 1000000 -> ShameTier.DRUNK_COMPASS
        distanceInMeters <= 5000000 -> ShameTier.COLUMBUS
        else -> ShameTier.FLAT_EARTHER
    }
    return roastsByTier[tier]?.random() ?: "I'm speechless."
}

/**
 * Berechnet den "Shame Score" basierend auf der Distanz.
 * Die Berechnung erfolgt in gestaffelten Bereichen, wobei die Punkte innerhalb jedes
 * Bereichs linear ansteigen. Je größer die Abweichung, desto mehr Punkte.
 *
 * @param distanceInMeters Die Distanz der Abweichung.
 * @return Der berechnete Shame Score als Integer.
 */
fun calculateShameScore(distanceInMeters: Double): Int {
    // Die Distanz wird für die Berechnung in Kilometern benötigt
    val distanceInKm = distanceInMeters / 1000.0

    return when {
        // Bereich 1: 0 bis 500 m -> 0–5 Punkte
        distanceInKm <= 0.5 -> {
            val percentage = distanceInKm / 0.5
            (percentage * 5).roundToInt()
        }

        // Bereich 2: 500 m bis 2 km -> 6–20 Punkte
        distanceInKm <= 2.0 -> {
            val percentage = (distanceInKm - 0.5) / 1.5
            val pointsToAdd = percentage * 14
            6 + pointsToAdd.roundToInt()
        }

        // Bereich 3: 2 km bis 25 km -> 21–100 Punkte
        distanceInKm <= 25.0 -> {
            val percentage = (distanceInKm - 2.0) / 23.0
            val pointsToAdd = percentage * 79
            21 + pointsToAdd.roundToInt()
        }

        // Bereich 4: 25 km bis 250 km -> 101–500 Punkte
        distanceInKm <= 250.0 -> {
            val percentage = (distanceInKm - 25.0) / 225.0
            val pointsToAdd = percentage * 399
            101 + pointsToAdd.roundToInt()
        }

        // Bereich 5: 250 km bis 500 km -> 501–1.000 Punkte
        distanceInKm <= 500.0 -> {
            val percentage = (distanceInKm - 250.0) / 250.0
            val pointsToAdd = percentage * 499
            501 + pointsToAdd.roundToInt()
        }

        // Bereich 6: 500 km bis 1.000 km -> 1.001–2.000 Punkte
        distanceInKm <= 1000.0 -> {
            val percentage = (distanceInKm - 500.0) / 500.0
            val pointsToAdd = percentage * 999
            1001 + pointsToAdd.roundToInt()
        }

        // Bereich 7: 1.000 km bis 5.000 km -> 2.001–5.000 Punkte
        distanceInKm <= 5000.0 -> {
            val percentage = (distanceInKm - 1000.0) / 4000.0
            val pointsToAdd = percentage * 2999
            2001 + pointsToAdd.roundToInt()
        }

        // Bereich 8: > 5.000 km -> 5.000+ Punkte
        else -> {
            5001 // Ein fester, hoher Wert für sehr große Abweichungen.
        }
    }
}