package com.example.unmapped.ui.recommended

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

/**
 * Stellt den Haupt-Gameplay-Bildschirm für den "Recommended Maps"-Modus dar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedScreen(
    navController: NavHostController,
    viewModel: RecommendedViewModel
) {
    val gameState by viewModel.gameState.collectAsState()
    var showGuessDialog by remember { mutableStateOf(false) }
    var guessPosition by remember { mutableStateOf<LatLng?>(null) }

    // Sicherheitsprüfung, falls die Spieldaten (noch) nicht verfügbar sind.
    if (gameState.gameRounds.isEmpty() || gameState.currentRoundIndex >= gameState.gameRounds.size) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    // Öffnet den Rate-Dialog automatisch, wenn der Timer abläuft.
    LaunchedEffect(gameState.forceGuess) { if (gameState.forceGuess && !showGuessDialog) { showGuessDialog = true } }

    val currentRound = gameState.gameRounds[gameState.currentRoundIndex]

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
                        text = "Round ${gameState.currentRoundIndex + 1} / ${gameState.gameRounds.size}",
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
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.RecommendedMapGraph.route) { inclusive = true } }
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
                if (!gameState.forceGuess) {
                    showGuessDialog = false
                    guessPosition = null
                    viewModel.resumeTimer()
                }
            },
            onGuessConfirmed = { confirmedPosition ->
                showGuessDialog = false
                val distanceInMeters = LocationUtils.calculateDistanceInMeters(currentRound.targetLocation, confirmedPosition)
                val shameScore = calculateShameScore(distanceInMeters)
                viewModel.submitGuess(guess = confirmedPosition, distance = distanceInMeters, score = shameScore)
                guessPosition = null
                navController.navigate(Screen.RecommendedMapResult.route)
            },
            isDismissible = !gameState.forceGuess
        )
    }
}