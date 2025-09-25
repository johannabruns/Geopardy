package com.example.unmapped.ui.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.unmapped.ui.components.ResultDetailsCard
import com.example.unmapped.ui.components.ResultHeaderCard
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import java.text.DecimalFormat

/**
 * Stellt den Ergebnisbildschirm nach einer Multiplayer-Runde dar.
 * Zeigt eine Karte mit den Schätzungen aller Spieler sowie eine detaillierte Punkteliste an.
 */
@Composable
fun MultiplayerResultScreen(viewModel: MultiplayerViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val currentRound = gameState.gameRounds.getOrNull(gameState.currentRoundIndex)

    if (currentRound == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val results = currentRound.results.entries.toList()
    val actualLocation = currentRound.targetLocation

    // Animiert die Kamera, um alle Schätzungen und den Zielort anzuzeigen.
    LaunchedEffect(results) {
        if (results.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder().include(actualLocation)
            results.forEach { resultEntry -> boundsBuilder.include(resultEntry.value.guessLocation) }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState) {
            // Marker für den tatsächlichen Ort (grün).
            Marker(
                state = MarkerState(position = actualLocation), title = "Actual Location",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
            // Marker für die Schätzung jedes Spielers.
            results.forEach { resultEntry ->
                val player = gameState.players.find { it.id == resultEntry.key }
                Marker(state = MarkerState(position = resultEntry.value.guessLocation), title = player?.name ?: "Unknown")
                Polyline(points = listOf(actualLocation, resultEntry.value.guessLocation), color = MaterialTheme.colorScheme.primary, width = 8f)
            }
        }

        ResultHeaderCard(
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Damage Report",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Text(
                    text = "Round ${gameState.currentRoundIndex + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }

        ResultDetailsCard(modifier = Modifier.align(Alignment.BottomCenter)) {
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                items(results) { resultEntry ->
                    val player = gameState.players.find { it.id == resultEntry.key }
                    PlayerResultRow(
                        playerName = player?.name ?: "???",
                        distanceKm = resultEntry.value.distanceInMeters / 1000,
                        score = resultEntry.value.shameScore
                    )
                    Divider()
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            val isLastRound = gameState.currentRoundIndex >= viewModel.gameState.value.gameRounds.size - 1
            Button(onClick = { viewModel.nextRound() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isLastRound) "Finish Game" else "Next Round")
            }
        }
    }
}

/**
 * Stellt eine einzelne Zeile in der Ergebnisliste dar, die das Ergebnis eines Spielers anzeigt.
 */
@Composable
private fun PlayerResultRow(playerName: String, distanceKm: Double, score: Int) {
    val df = remember { DecimalFormat("#,##0") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(playerName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(
            "${df.format(distanceKm)} km",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            "+$score",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
    }
}