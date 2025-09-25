package com.example.unmapped.ui.recommended

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.unmapped.R
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.components.GameLoadingScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Stellt den Ladebildschirm für eine empfohlene Karte dar. Zeigt eine Nachricht an,
 * falls der Spieler die Karte bereits abgeschlossen hat.
 */
@Composable
fun RecommendedLoadingScreen(
    navController: NavHostController,
    viewModel: RecommendedViewModel
) {
    // Wählt ein passendes Zitat basierend auf der mapId aus.
    val quote = when (viewModel.mapId) {
        "tourist_traps" -> "Hope you like crowds, sweat and selfie sticks."
        "pop_culture_hotspots" -> "Don’t trip over the fangirls."
        "cancelled_destinations" -> "So… we don’t really go here anymore."
        "conspiracy_core" -> "Tin foil hats on, bestie."
        else -> "You must really hate yourself to pick this mode."
    }

    // Wählt ein passendes Hintergrundbild basierend auf der mapId aus.
    val backgroundImageRes = when (viewModel.mapId) {
        "tourist_traps" -> R.drawable.card_tourist
        "pop_culture_hotspots" -> R.drawable.card_popculture
        "cancelled_destinations" -> R.drawable.card_cancelled
        "conspiracy_core" -> R.drawable.card_conspiracy
        else -> R.drawable.card_single
    }

    var showCompletedMessage by remember { mutableStateOf(false) }

    // Startet den Ladevorgang und die Navigation.
    LaunchedEffect(key1 = Unit) {
        val startTime = System.currentTimeMillis()
        val minDisplayTime = 2000L
        viewModel.startNewGame()

        // Wartet, bis das Spiel geladen ist.
        val loadedState = viewModel.gameState.first { !it.isLoading }

        // Prüft, ob es überhaupt Runden zum Spielen gibt. Wenn nicht, hat der Spieler die Karte bereits abgeschlossen.
        if (loadedState.gameRounds.isEmpty()) {
            showCompletedMessage = true
            return@LaunchedEffect
        }

        // Stellt eine Mindestanzeigedauer sicher und navigiert dann zum Spielbildschirm.
        val elapsedTime = System.currentTimeMillis() - startTime
        val remainingTime = minDisplayTime - elapsedTime
        if (remainingTime > 0) {
            delay(remainingTime)
        }
        navController.navigate(Screen.RecommendedMapPlay.route) {
            popUpTo(Screen.RecommendedMapLoading.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GameLoadingScreen(
            backgroundImageRes = backgroundImageRes
        ) {
            // Zeigt entweder die "Abgeschlossen"-Nachricht oder die Ladeanzeige an.
            if (showCompletedMessage) {
                Text(
                    "This map is already a completed disaster.",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Please select another one to humiliate yourself.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RecommendedMapGraph.route) { inclusive = true }
                    }
                }) {
                    Text("Back to Home")
                }
            } else {
                Text(
                    text = "LOADING MAP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = quote,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}