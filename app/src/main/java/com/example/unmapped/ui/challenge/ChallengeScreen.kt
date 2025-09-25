package com.example.unmapped.ui.challenge

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
import com.example.unmapped.data.calculateShameScore
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.components.GameBottomButton
import com.example.unmapped.ui.components.GameExitButton
import com.example.unmapped.ui.components.GameInfoCard
import com.example.unmapped.ui.guess.GuessOverlay
import com.example.unmapped.ui.singleplayer.ExitConfirmationDialog
import com.example.unmapped.ui.singleplayer.GoogleStreetView
import com.example.unmapped.utils.LocationUtils
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    navController: NavHostController,
    viewModel: ChallengeViewModel
) {
    val gameState by viewModel.gameState.collectAsState()
    var showGuessDialog by remember { mutableStateOf(false) }
    var guessPosition by remember { mutableStateOf<LatLng?>(null) }

    // Dieser Effekt öffnet automatisch den Rate-Dialog, wenn der Timer abläuft.
    LaunchedEffect(gameState.forceGuess) { if (gameState.forceGuess) { showGuessDialog = true } }

    // Sicherheitsprüfung: Zeigt einen Ladekreis an, falls die Spieldaten noch nicht bereit sind.
    if (gameState.gameRounds.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val currentRound = gameState.gameRounds[gameState.currentRoundIndex]

    Scaffold(
        containerColor = Color.Transparent, // Macht den Scaffold-Hintergrund durchsichtig.
        topBar = {
            // Die obere Leiste mit Exit-Button, Timer und Rundenanzeige.
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
                        text = "Disaster ${gameState.currentRoundIndex + 1} of 5",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        bottomBar = {
            // Die untere Leiste mit dem Button zum Öffnen der Karte.
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
        // Die Street View Komponente füllt den gesamten Hintergrund.
        GoogleStreetView(
            modifier = Modifier.fillMaxSize(),
            startCoordinates = currentRound.targetLocation,
            isNavigationEnabled = gameState.isMovementAllowed
        )

        // Zeigt den Dialog zum Bestätigen des Spielabbruchs an, falls nötig.
        if (gameState.showExitDialog) {
            ExitConfirmationDialog(
                onConfirm = {
                    viewModel.onExitConfirmed()
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.ChallengeGraph.route) { inclusive = true } }
                },
                onDismiss = { viewModel.onExitDismissed() }
            )
        }
    }

    // Das Karten-Overlay zur Abgabe einer Schätzung.
    if (showGuessDialog) {
        GuessOverlay(
            markerPosition = guessPosition,
            onMapClick = { newPosition -> guessPosition = newPosition },
            onDismissRequest = {
                // Nur wenn der Timer nicht abgelaufen ist, kann der Dialog abgebrochen werden.
                showGuessDialog = false
                guessPosition = null
                viewModel.resumeTimer()
            },
            onGuessConfirmed = { confirmedPosition ->
                // Wenn die Schätzung bestätigt wird, werden Distanz und Score berechnet...
                showGuessDialog = false
                val distanceInMeters = LocationUtils.calculateDistanceInMeters(currentRound.targetLocation, confirmedPosition)
                val shameScore = calculateShameScore(distanceInMeters)
                // ...an das ViewModel übergeben...
                viewModel.submitGuess(guess = confirmedPosition, distance = distanceInMeters, score = shameScore)
                guessPosition = null
                // ...und zum Ergebnisbildschirm navigiert.
                navController.navigate(Screen.ChallengeResult.route)
            },
            // Der Dialog kann nicht geschlossen werden, wenn der Timer abgelaufen ist.
            isDismissible = !gameState.forceGuess
        )
    }
}