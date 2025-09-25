package com.example.unmapped.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Verwaltet die Speicherung und den Abruf von Spielerdaten.
 * Alle Spieler werden als eine einzige JSON-Liste in den SharedPreferences gespeichert.
 *
 * @param context Der Anwendungskontext, um auf SharedPreferences zugreifen zu können.
 */

class PlayerRepository(context: Context) {
    // SharedPreferences-Instanz für die Speicherung der Spielerdaten.
    private val sharedPreferences = context.getSharedPreferences("player_repository", Context.MODE_PRIVATE)

    // GSON-Instanz zur Konvertierung von Objekten in JSON und umgekehrt.
    private val gson = Gson()

    // Der Schlüssel, unter dem die Spielerliste in den SharedPreferences gespeichert wird.
    private val playersKey = "players_list"


    /**
     * Speichert eine Liste von Spielern in den SharedPreferences.
     * Die Liste wird zuerst in einen JSON-String konvertiert.
     *
     * @param players Die Liste der zu speichernden Spieler.
     */
    fun savePlayers(players: List<Player>) {
        val json = gson.toJson(players)
        sharedPreferences.edit().putString(playersKey, json).apply()
    }

    /**
     * Ruft die vollständige Liste aller Spieler aus den SharedPreferences ab.
     *
     * @return Eine veränderbare Liste von Spielern. Gibt eine leere Liste zurück, wenn keine Daten vorhanden sind.
     */
    fun getAllPlayers(): MutableList<Player> {
        val json = sharedPreferences.getString(playersKey, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Player>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    /**
     * Findet einen Spieler anhand seiner einzigartigen ID.
     *
     * @param id Die ID des gesuchten Spielers.
     * @return Das gefundene Spieler-Objekt oder null, wenn kein Spieler mit dieser ID existiert.
     */
    fun getPlayerById(id: String): Player? {
        return getAllPlayers().find { it.id == id }
    }

    /**
     * Findet einen Spieler anhand seines Namens (ignoriert Groß- und Kleinschreibung).
     *
     * @param name Der Name des gesuchten Spielers.
     * @return Das gefundene Spieler-Objekt oder null, wenn kein Spieler mit diesem Namen existiert.
     */
    fun getPlayerByName(name: String): Player? {
        return getAllPlayers().find { it.name.equals(name, ignoreCase = true) }
    }

    /**
     * Aktualisiert die Daten eines Spielers oder fügt ihn hinzu, falls er nicht existiert.
     *
     * @param playerToUpdate Das Spieler-Objekt mit den aktualisierten Daten.
     */
    fun updatePlayer(playerToUpdate: Player) {
        val players = getAllPlayers()
        val index = players.indexOfFirst { it.id == playerToUpdate.id }
        if (index != -1) {
            players[index] = playerToUpdate
        } else {
            players.add(playerToUpdate)
        }
        savePlayers(players)
    }

    /**
     * Fügt einem bestimmten Spieler Punkte zu seinem Gesamt-Shame-Score hinzu.
     *
     * @param playerId Die ID des Spielers, dessen Punktestand aktualisiert werden soll.
     * @param scoreToAdd Die Anzahl der Punkte, die hinzugefügt werden sollen.
     */
    fun addScoreToPlayer(playerId: String, scoreToAdd: Int) {
        val players = getAllPlayers()
        val player = players.find { it.id == playerId }
        // 'let' wird nur ausgeführt, wenn der Spieler nicht null ist.
        player?.let {
            it.totalShameScore += scoreToAdd
            // Speichert die gesamte Spielerliste mit dem aktualisierten Punktestand.
            savePlayers(players)
        }
    }

    /**
     * Löscht einen Spieler anhand seiner ID aus der Liste.
     *
     * @param playerId Die ID des zu löschenden Spielers.
     */
    fun deletePlayer(playerId: String) {
        val players = getAllPlayers()
        // 'removeAll' entfernt alle Elemente, die der Bedingung entsprechen, und gibt 'true' zurück, wenn etwas entfernt wurde.
        val removed = players.removeAll { it.id == playerId }
        if (removed) {
            savePlayers(players)
        }
    }
}