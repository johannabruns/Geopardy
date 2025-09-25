package com.example.unmapped.ui.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

@Composable
fun ChallengeLoadingScreen(
    navController: NavHostController,
    challengeViewModel: ChallengeViewModel
) {
    // Wählt ein zufälliges, spöttisches Zitat für den Ladebildschirm aus.
    val quote = remember {
        listOf(
            "You really think you’re that good?",
            "Bold of you to trust your skills like that.",
            "Confidence is cute… let’s see if it lasts.",
            "So you woke up and chose overconfidence?",
            "Love the energy, can’t wait to see the flop.",
            "You must really hate yourself to pick this mode."
        ).random()
    }

    // Dieser Effekt startet, sobald der Screen angezeigt wird, und steuert den Lade- und Navigationsprozess.
    LaunchedEffect(key1 = Unit) {
        val startTime = System.currentTimeMillis()
        val minDisplayTime = 2000L // Mindestanzeigedauer von 2 Sekunden, um ein Flackern zu vermeiden.

        // Startet die Spielvorbereitung im ViewModel.
        challengeViewModel.startNewGame()

        // Wartet, bis der ViewModel signalisiert, dass das Spiel geladen und bereit ist.
        challengeViewModel.gameState.first { !it.isLoading && it.gameRounds.isNotEmpty() }

        // Stellt sicher, dass der Ladebildschirm für mindestens `minDisplayTime` sichtbar ist.
        val elapsedTime = System.currentTimeMillis() - startTime
        val remainingTime = minDisplayTime - elapsedTime
        if (remainingTime > 0) {
            delay(remainingTime)
        }

        // Navigiert zum Spielbildschirm und entfernt den Ladebildschirm aus dem Backstack.
        navController.navigate(Screen.ChallengePlay.route) {
            popUpTo(Screen.ChallengeLoading.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GameLoadingScreen(
            backgroundImageRes = R.drawable.card_challenge
        ) {
            Text(
                text = "INCOMING CHALLENGE",
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