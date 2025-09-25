package com.example.unmapped.ui.multiplayer

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unmapped.data.Player
import com.example.unmapped.data.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * Repräsentiert den UI-Zustand des Multiplayer-Setup-Bildschirms.
 *
 * @property mainPlayer Der eingeloggte Hauptspieler.
 * @property allOtherPlayers Eine Liste aller anderen verfügbaren Spieler.
 * @property selectedPlayerIds Ein Set der IDs aller für das Spiel ausgewählten Spieler.
 * @property showCreatePlayerDialog Steuert die Sichtbarkeit des Dialogs zum Erstellen neuer Spieler.
 * @property playerToDelete Hält das Spieler-Objekt, für das eine Löschbestätigung angefordert wurde.
 */
data class MultiplayerSetupState(
    val mainPlayer: Player? = null,
    val allOtherPlayers: List<Player> = emptyList(),
    val selectedPlayerIds: Set<String> = emptySet(),
    val showCreatePlayerDialog: Boolean = false,
    val playerToDelete: Player? = null
)

class MultiplayerSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val playerRepository = PlayerRepository(application)
    private val profilePrefs = application.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(MultiplayerSetupState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPlayers()
    }

    /**
     * Lädt den Hauptspieler sowie alle anderen Spieler aus dem Repository und initialisiert den UI-Zustand.
     */
    private fun loadPlayers() {
        viewModelScope.launch {
            val mainPlayerId = profilePrefs.getString("main_player_id", null)
            val mainPlayer = mainPlayerId?.let { playerRepository.getPlayerById(it) }

            if (mainPlayer != null) {
                val otherPlayers = playerRepository.getAllPlayers().filter { it.id != mainPlayer.id }
                _uiState.update {
                    it.copy(
                        mainPlayer = mainPlayer,
                        allOtherPlayers = otherPlayers,
                        selectedPlayerIds = setOf(mainPlayer.id) // Der Hauptspieler ist immer vorausgewählt.
                    )
                }
            } else {
                Log.e("SetupViewModel", "Main player could not be loaded!")
            }
        }
    }

    /**
     * Fügt einen Spieler zur Auswahl hinzu oder entfernt ihn.
     * @param playerId Die ID des Spielers, dessen Auswahlstatus geändert werden soll.
     */
    fun togglePlayerSelection(playerId: String) {
        _uiState.update { currentState ->
            val newSelectedIds = currentState.selectedPlayerIds.toMutableSet()
            if (newSelectedIds.contains(playerId)) {
                newSelectedIds.remove(playerId)
            } else {
                newSelectedIds.add(playerId)
            }
            currentState.copy(selectedPlayerIds = newSelectedIds)
        }
    }

    /**
     * Erstellt einen neuen Spieler, speichert ihn und aktualisiert die Spielerliste.
     * @param name Der Name des neuen Spielers.
     * @param tempImageUri Die temporäre URI des ausgewählten Bildes.
     */
    fun createNewPlayer(name: String, tempImageUri: Uri?) {
        viewModelScope.launch {
            val newPlayerId = UUID.randomUUID().toString()
            var permanentImageUri: Uri? = null

            if (tempImageUri != null) {
                permanentImageUri = copyUriToPermanentStorage(tempImageUri, newPlayerId)
            }

            val newPlayer = Player(id = newPlayerId, name = name, imageUri = permanentImageUri?.toString())
            playerRepository.updatePlayer(newPlayer)
            loadPlayers() // Lädt die Spielerliste neu, um den neuen Spieler sofort anzuzeigen.
            onDismissCreatePlayerDialog()
        }
    }

    /**
     * Gibt eine Liste der aktuell ausgewählten Spieler-Objekte zurück.
     */
    fun getSelectedPlayers(): List<Player> {
        val state = _uiState.value
        val allAvailablePlayers = (state.allOtherPlayers + state.mainPlayer!!).distinctBy { it.id }
        return allAvailablePlayers.filter { state.selectedPlayerIds.contains(it.id) }
    }

    /**
     * Öffnet den Dialog zur Erstellung eines neuen Spielers.
     */
    fun onShowCreatePlayerDialog() {
        _uiState.update { it.copy(showCreatePlayerDialog = true) }
    }

    /**
     * Schließt den Dialog zur Erstellung eines neuen Spielers.
     */
    fun onDismissCreatePlayerDialog() {
        _uiState.update { it.copy(showCreatePlayerDialog = false) }
    }

    /**
     * Fordert die Bestätigung zur Löschung eines Spielers an.
     * @param player Der Spieler, der gelöscht werden soll.
     */
    fun requestPlayerDeletion(player: Player) {
        if (player.id != _uiState.value.mainPlayer?.id) {
            _uiState.update { it.copy(playerToDelete = player) }
        }
    }

    /**
     * Bestätigt und führt die Löschung eines Spielers durch.
     */
    fun confirmPlayerDeletion() {
        viewModelScope.launch {
            _uiState.value.playerToDelete?.let { player ->
                playerRepository.deletePlayer(player.id)
                loadPlayers() // Lade Spieler neu, um die Liste zu aktualisieren.
                _uiState.update { it.copy(playerToDelete = null, selectedPlayerIds = it.selectedPlayerIds - player.id) }
            }
        }
    }

    /**
     * Bricht den Löschvorgang ab.
     */
    fun cancelPlayerDeletion() {
        _uiState.update { it.copy(playerToDelete = null) }
    }

    /**
     * Kopiert eine Bilddatei von einer temporären URI in den permanenten App-Speicher.
     * @param sourceUri Die temporäre URI vom Photo Picker.
     * @param playerId Die ID des Spielers, zu dem das Bild gehört.
     * @return Die neue, permanente URI der gespeicherten Datei.
     */
    private fun copyUriToPermanentStorage(sourceUri: Uri, playerId: String): Uri? {
        val context = getApplication<Application>().applicationContext
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val permanentDir = File(context.filesDir, "profile_images").apply { mkdirs() }
            val permanentFile = File(permanentDir, "$playerId.jpg")

            inputStream?.use { input ->
                permanentFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(permanentFile)
        } catch (e: Exception) {
            Log.e("SetupViewModel", "Error copying file", e)
            null
        }
    }
}