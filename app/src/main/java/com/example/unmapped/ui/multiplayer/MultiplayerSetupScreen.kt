package com.example.unmapped.ui.multiplayer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.unmapped.R
import com.example.unmapped.data.Player
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.theme.LightPurple
import com.example.unmapped.ui.theme.MediumPurple

/**
 * Stellt den Bildschirm zur Einrichtung eines Multiplayer-Spiels dar.
 * Hier können Spieler ausgewählt, erstellt und gelöscht werden.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiPlayerSetupScreen(
    navController: NavHostController,
    viewModel: MultiplayerSetupViewModel = viewModel(
        factory = MultiplayerViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPlayers = viewModel.getSelectedPlayers()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Lässt am unteren Rand Platz für den schwebenden Aktionsbutton (FAB).
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header-Element
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MediumPurple,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = "Loose Together",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(vertical = 32.dp)
                    )
                }
            }

            // Sektion für den Hauptspieler
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("Your Mediocre Self", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                uiState.mainPlayer?.let { mainPlayer ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PlayerProfileCard(
                            player = mainPlayer, isSelected = true, isMainPlayer = true,
                            onSelect = {}, onLongClick = {},
                            modifier = Modifier.fillMaxWidth(0.5f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sektion für die Auswahl der Gegner
            item {
                SectionTitle("Pick Some Fellow Fools", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Grid-Layout für die Gegner und den "Hinzufügen"-Button
            val opponentsAndAddButton = uiState.allOtherPlayers + listOf(null) // `null` repräsentiert den "Add"-Button.
            val rows = opponentsAndAddButton.chunked(3) // Teilt die Liste in Reihen mit je 3 Elementen.
            items(rows.size) { rowIndex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (i in 0 until 3) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (i < rows[rowIndex].size) {
                                val player = rows[rowIndex][i]
                                if (player != null) {
                                    PlayerProfileCard(
                                        player = player,
                                        isSelected = uiState.selectedPlayerIds.contains(player.id),
                                        onSelect = { viewModel.togglePlayerSelection(player.id) },
                                        onLongClick = { viewModel.requestPlayerDeletion(player) }
                                    )
                                } else {
                                    AddPlayerCard(onClick = { viewModel.onShowCreatePlayerDialog() })
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Der schwebende Aktionsbutton (FAB) zum Starten des Spiels.
        ExtendedFloatingActionButton(
            onClick = {
                if (selectedPlayers.size >= 2) {
                    // Übergibt die ausgewählte Spielerliste an den nächsten Navigationsgraphen.
                    navController.currentBackStackEntry?.savedStateHandle?.set("players", selectedPlayers)
                    navController.navigate(Screen.MultiplayerGameGraph.route)
                }
            },
            text = { Text("Start Game (${selectedPlayers.size})", fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Check, contentDescription = "Dive Into Disaster") },
            expanded = true,
            // Ändert die Farbe, je nachdem ob genügend Spieler ausgewählt sind.
            containerColor = if (selectedPlayers.size >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selectedPlayers.size >= 2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )
    }

    if (uiState.showCreatePlayerDialog) {
        CreatePlayerDialog(
            onDismiss = { viewModel.onDismissCreatePlayerDialog() },
            onSave = { name, uri -> viewModel.createNewPlayer(name, uri) }
        )
    }

    uiState.playerToDelete?.let { player ->
        DeleteConfirmationDialog(
            player = player,
            onDismiss = { viewModel.cancelPlayerDeletion() },
            onConfirm = { viewModel.confirmPlayerDeletion() }
        )
    }
}

/**
 * Stellt einen formatierten Titel für eine Sektion dar.
 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = LightPurple
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Stellt eine Karte für ein Spielerprofil dar. Unterstützt Auswahl und langes Drücken zum Löschen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerProfileCard(
    player: Player, isSelected: Boolean, onSelect: () -> Unit,
    onLongClick: () -> Unit, isMainPlayer: Boolean = false, modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    Card(
        modifier = modifier.combinedClickable(
            enabled = !isMainPlayer,
            onClick = onSelect,
            onLongClick = onLongClick
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = player.imageUri?.toUri() ?: R.drawable.icon_gamer_black,
                    contentDescription = player.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(4.dp, borderColor, CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.icon_gamer_black)
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check, contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isMainPlayer) "You" else player.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Zeigt einen Bestätigungsdialog vor dem Löschen eines Spielers an.
 */
@Composable
private fun DeleteConfirmationDialog(player: Player, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Player") },
        text = { Text("Are you sure you want to delete ${player.name}? This action cannot be undone.") },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/**
 * Stellt eine Karte dar, die das Hinzufügen eines neuen Spielers ermöglicht.
 */
@Composable
fun AddPlayerCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .defaultMinSize(minHeight = 144.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Add new player",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Add New", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Zeigt einen Dialog zum Erstellen eines neuen Spielers mit Namenseingabe und Bildauswahl an.
 */
@Composable
fun CreatePlayerDialog(onDismiss: () -> Unit, onSave: (String, Uri?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isSaveEnabled = name.isNotBlank()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header des Dialogs
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MediumPurple,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = "Create Player",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create New Player",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Inhaltsbereich mit Eingabefeldern
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Bildauswahl
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        if (imageUri == null) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.CameraAlt,
                                    "Select Image",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    // Namens-Eingabefeld
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Who's this?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Button-Leiste am Ende des Dialogs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Cancel", fontSize = 16.sp)
                    }
                    Button(
                        onClick = { onSave(name, imageUri) },
                        enabled = isSaveEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Save", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}