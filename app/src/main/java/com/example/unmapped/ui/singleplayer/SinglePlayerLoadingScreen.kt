package com.example.unmapped.ui.singleplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.unmapped.R
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.components.GameLoadingScreen

/**
 * Stellt den Ladebildschirm für den Einzelspielermodus dar.
 */
@Composable
fun SinglePlayerLoadingScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val gameState by gameViewModel.gameState.collectAsState()

    // Startet das Laden des Spiels, sobald der Screen angezeigt wird.
    LaunchedEffect(key1 = Unit) {
        gameViewModel.startNewGame()
    }

    // Navigiert zum Spielbildschirm, sobald der Ladevorgang im ViewModel abgeschlossen ist.
    LaunchedEffect(gameState.isLoading) {
        if (!gameState.isLoading && gameState.gameRounds.isNotEmpty()) {
            navController.navigate(Screen.GamePlay.route) {
                popUpTo(Screen.GameLoading.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GameLoadingScreen(
            backgroundImageRes = R.drawable.card_single
        ) {
            Text(
                text = "PREPARING YOUR HUMILIATION",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Let’s see how wrong you can be this time.",
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