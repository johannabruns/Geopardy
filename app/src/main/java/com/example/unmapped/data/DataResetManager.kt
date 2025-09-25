package com.example.unmapped.data

import android.content.Context
import java.io.File

/**
 * Ein Singleton-Objekt, das eine zentrale Funktion zum Löschen aller relevanten
 * Anwendungsdaten bietet, um die App in den Ausgangszustand zurückzusetzen.
 */
object DataResetManager {

    /**
     * Löscht alle gespeicherten Benutzerdaten, einschließlich Spieler, Badges, Profileinstellungen und Bilder.
     *
     * @param context Der Anwendungskontext, um auf SharedPreferences und das Dateisystem zuzugreifen.
     */
    fun resetAllData(context: Context) {
        // 1. Löscht alle Spielerdaten aus 'player_repository.xml'.
        val playerPrefs = context.getSharedPreferences("player_repository", Context.MODE_PRIVATE)
        playerPrefs.edit().clear().apply()

        // 2. Löscht alle Profil-spezifischen Daten (z.B. ID des Hauptspielers) aus 'profile_prefs.xml'.
        val profilePrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        profilePrefs.edit().clear().apply()

        // 3. Löscht alle gespeicherten Badge-Fortschritte aus 'badge_prefs.xml'.
        val badgePrefs = context.getSharedPreferences("badge_prefs", Context.MODE_PRIVATE)
        badgePrefs.edit().clear().apply()

        // 4. Löscht das Verzeichnis, in dem die Profilbilder gespeichert sind.
        val imageDir = File(context.filesDir, "profile_images")
        if (imageDir.exists()) {
            imageDir.deleteRecursively() // Löscht das Verzeichnis und alle darin enthaltenen Dateien.
        }

        // Hinweis: Die 'app_prefs' werden absichtlich nicht gelöscht,
        // damit der "is_first_run"-Flag für die Erstanzeige des Onboardings erhalten bleibt.
    }
}