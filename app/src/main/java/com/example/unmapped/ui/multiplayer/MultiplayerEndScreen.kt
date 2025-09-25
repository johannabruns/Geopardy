package com.example.unmapped.ui.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.unmapped.data.Player
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.theme.MediumPurple

/**
 * Stellt den Endbildschirm eines Multiplayer-Spiels dar.
 * Zeigt die finale Rangliste aller Spieler an, wobei der Gewinner (mit der niedrigsten Punktzahl) hervorgehoben wird.
 */
@Composable
fun MultiplayerEndScreen(navController: NavHostController, viewModel: MultiplayerViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    // Berechnet die Gesamtpunktzahl für jeden Spieler und sortiert die Liste, um die Rangliste zu erstellen.
    val finalScores = gameState.players.map { player ->
        val totalScore = gameState.gameRounds.sumOf { round ->
            round.results[player.id]?.shameScore ?: 0
        }
        player to totalScore
    }.sortedBy { it.second } // Sortiert aufsteigend nach Punkten.

    val winner = finalScores.firstOrNull()
    val remainingPlayers = finalScores.drop(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MultiplayerEndHeader(winnerName = winner?.first?.name)

        LazyColumn(
            modifier = Modifier
                .weight(1f) // Nimmt den gesamten verfügbaren Platz zwischen Header und Buttons ein.
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Karte für den Gewinner
            item {
                winner?.let { (player, score) ->
                    WinnerCard(player = player, totalScore = score)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Karte für die restlichen Spieler
            if (remainingPlayers.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            remainingPlayers.forEachIndexed { index, (player, score) ->
                                RankingRow(rank = index + 2, player = player, totalScore = score)
                                if (index < remainingPlayers.lastIndex) {
                                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Untere Button-Leiste
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    navController.navigate(Screen.MultiPlayerSetup.route) {
                        popUpTo(Screen.MultiPlayerSetup.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text("try harder", fontSize = 16.sp)
            }
            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text("leave", fontSize = 16.sp)
            }
        }
    }
}

/**
 * Stellt den Header für den Endbildschirm dar.
 */
@Composable
private fun MultiplayerEndHeader(winnerName: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MediumPurple,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents, contentDescription = "Trophy",
                modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "The embarrassment ends.", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = winnerName?.let { "Congrats, $it. You were the least wrong." } ?: "Final Rankings",
                style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * Stellt eine speziell formatierte Karte für den Gewinner dar.
 */
@Composable
private fun WinnerCard(player: Player, totalScore: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        RankingRow(
            rank = 1,
            player = player,
            totalScore = totalScore,
            isWinner = true
        )
    }
}

/**
 * Stellt eine einzelne Zeile in der Rangliste dar.
 */
@Composable
private fun RankingRow(rank: Int, player: Player, totalScore: Int, isWinner: Boolean = false) {
    val contentColor = if (isWinner) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank.",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.width(40.dp),
            color = if (isWinner) contentColor else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = player.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Text(
            text = "$totalScore",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isWinner) contentColor else MaterialTheme.colorScheme.primary
        )
    }
}