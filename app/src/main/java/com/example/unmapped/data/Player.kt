package com.example.unmapped.data

import java.util.UUID

/**
 * Repräsentiert einen einzelnen Spieler im Spiel.
 *
 * @property id Eine einzigartige, automatisch generierte ID zur Identifizierung des Spielers.
 * @property name Der vom Benutzer gewählte Name des Spielers.
 * @property imageUri Der Pfad (als String) zum Profilbild des Spielers, kann null sein.
 * @property totalShameScore Die Summe aller "Shame Scores", die der Spieler über alle Runden gesammelt hat.
 */

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val imageUri: String? = null,
    var totalShameScore: Int = 0
)
