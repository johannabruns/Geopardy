package com.example.unmapped.ui.multiplayer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.unmapped.R

/**
 * Stellt einen Übergangsbildschirm dar, der vor dem Spielstart oder zwischen den Zügen
 * eines Spielers angezeigt wird, um anzukündigen, wer als Nächstes dran ist.
 *
 * @param viewModel Der [MultiplayerViewModel], der den aktuellen Spielzustand liefert.
 */
@Composable
fun MultiplayerTransitionScreen(viewModel: MultiplayerViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    // Ermittelt den anzuzeigenden Text und Spieler basierend auf der aktuellen Spielphase.
    val (titleText, playerToShow) = when (gameState.phase) {
        GamePhase.GAME_START -> "First up is..." to gameState.players.getOrNull(0)
        GamePhase.ROUND_TRANSITION -> "Next up is..." to gameState.players.getOrNull(gameState.currentPlayerIndex + 1)
        else -> null to null
    }

    // Sicherheitsprüfung, falls kein Spieler angezeigt werden kann.
    if (playerToShow == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading next turn...")
        }
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    titleText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    AsyncImage(
                        model = playerToShow.imageUri?.toUri() ?: R.drawable.icon_gamer_black,
                        contentDescription = playerToShow.name,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(6.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.icon_gamer_black)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = playerToShow.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { viewModel.nextTurn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                val buttonText = if (gameState.phase == GamePhase.GAME_START) "Dive Into Disaster" else "Unleash The Chaos"
                Text(buttonText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}