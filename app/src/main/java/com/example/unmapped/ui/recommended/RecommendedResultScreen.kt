package com.example.unmapped.ui.recommended

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.unmapped.data.getRandomRoast
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.components.ResultDetailsCard
import com.example.unmapped.ui.components.ResultHeaderCard
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import java.text.DecimalFormat

/**
 * Stellt den Ergebnisbildschirm nach einer einzelnen Runde im "Recommended Maps"-Modus dar.
 */
@Composable
fun RecommendedResultScreen(
    navController: NavHostController,
    viewModel: RecommendedViewModel
) {
    val gameState by viewModel.gameState.collectAsState()

    // Sicherheitsprüfung, um sicherzustellen, dass gültige Ergebnisdaten vorhanden sind.
    if (gameState.gameRounds.isEmpty() ||
        gameState.currentRoundIndex >= gameState.gameRounds.size ||
        gameState.gameRounds[gameState.currentRoundIndex].result == null
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val result = gameState.gameRounds[gameState.currentRoundIndex].result!!
    val cameraPositionState = rememberCameraPositionState()
    val roast = getRandomRoast(result.distanceInMeters)
    val df = remember { DecimalFormat("#,##0") }

    // Animiert die Kamera, um sowohl den Zielort als auch die Schätzung anzuzeigen.
    LaunchedEffect(result) {
        val bounds = LatLngBounds.builder()
            .include(result.actualLocation)
            .include(result.guessLocation)
            .build()
        delay(100)
        cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 150))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(state = MarkerState(position = result.actualLocation), title = "Actual Location")
            Marker(state = MarkerState(position = result.guessLocation), title = "Your Guess")
            Polyline(points = listOf(result.actualLocation, result.guessLocation), color = MaterialTheme.colorScheme.primary, width = 8f)
        }

        ResultHeaderCard(
            modifier = Modifier.align(Alignment.TopCenter),
            roastText = roast
        )

        ResultDetailsCard(modifier = Modifier.align(Alignment.BottomCenter)) {
            Text(
                text = "${df.format(result.distanceInMeters / 1000)} km off",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            val scoreText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append("That’s ")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)) {
                    append("${result.shameScore} points")
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append(" added to your total amount of failure.")
                }
            }
            Text(text = scoreText, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            // Prüft, ob es die letzte Runde ist, um den Button-Text und die Aktion anzupassen.
            val isLastRound = gameState.currentRoundIndex >= gameState.gameRounds.size - 1
            Button(
                onClick = {
                    if (isLastRound) {
                        viewModel.nextRound()
                        navController.navigate(Screen.RecommendedMapEnd.route) {
                            popUpTo(Screen.RecommendedMapPlay.route) { inclusive = true }
                        }
                    } else {
                        viewModel.nextRound()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (isLastRound) "Finish Game" else "Next Round", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}