package com.example.unmapped.ui.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.components.GameBottomButton
import com.example.unmapped.ui.components.GameExitButton
import com.example.unmapped.ui.components.GameInfoCard
import com.example.unmapped.ui.guess.GuessOverlay
import com.example.unmapped.ui.singleplayer.GoogleStreetView
import com.google.android.gms.maps.model.LatLng

/**
 * Stellt den Haupt-Gameplay-Bildschirm für den Multiplayer-Modus dar.
 * Zeigt die Street View-Ansicht, den Timer und Informationen zum aktuellen Spieler und zur Runde an.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerGameScreen(
    navController: NavHostController,
    viewModel: MultiplayerViewModel
) {
    val gameState by viewModel.gameState.collectAsState()
    var showGuessDialog by remember { mutableStateOf(false) }
    var guessPosition by remember { mutableStateOf<LatLng?>(null) }

    // Öffnet den Rate-Dialog automatisch, wenn der Timer abläuft.
    LaunchedEffect(gameState.forceGuess) { if (gameState.forceGuess) { showGuessDialog = true } }

    // Sicherheitsprüfung, falls die Spieldaten noch nicht bereit sind.
    if (gameState.gameRounds.isEmpty() || gameState.players.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val currentRound = gameState.gameRounds[gameState.currentRoundIndex]
    val currentPlayer = gameState.players[gameState.currentPlayerIndex]
    val totalRounds = viewModel.gameState.value.gameRounds.size

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameExitButton(onClick = { viewModel.onExitAttempt() })
                GameInfoCard {
                    Text(
                        text = gameState.formattedTime,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                GameInfoCard {
                    Text(
                        text = "${currentPlayer.name} | ${gameState.currentRoundIndex + 1}/$totalRounds",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                GameBottomButton(onClick = {
                    viewModel.pauseTimer()
                    showGuessDialog = true
                })
            }
        }
    ) { scaffoldPadding ->
        GoogleStreetView(
            modifier = Modifier.fillMaxSize(),
            startCoordinates = currentRound.targetLocation,
            isNavigationEnabled = gameState.isMovementAllowed
        )

        if (gameState.showExitDialog) {
            ExitConfirmationDialog(
                onConfirm = {
                    viewModel.onExitConfirmed()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.MultiplayerGameGraph.route) { inclusive = true }
                    }
                },
                onDismiss = { viewModel.onExitDismissed() }
            )
        }
    }

    if (showGuessDialog) {
        GuessOverlay(
            markerPosition = guessPosition,
            onMapClick = { newPosition -> guessPosition = newPosition },
            onDismissRequest = {
                showGuessDialog = false
                guessPosition = null
                viewModel.onExitDismissed() // Setzt den Timer fort.
            },
            onGuessConfirmed = { confirmedPosition ->
                showGuessDialog = false
                viewModel.submitGuess(confirmedPosition)
                guessPosition = null
            },
            isDismissible = !gameState.forceGuess
        )
    }
}

/**
 * Zeigt einen Bestätigungsdialog an, wenn der Spieler versucht, das Spiel zu verlassen.
 */
@Composable
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave Game?") },
        text = { Text("Your current progress in this round will be lost. Are you sure?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Leave") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Continue") } }
    )
}