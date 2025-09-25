package com.example.unmapped.ui.rankings

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.core.net.toUri
import coil.compose.SubcomposeAsyncImage
import com.example.unmapped.data.Player
import com.example.unmapped.ui.theme.LightPurple

/**
 * Stellt den Ranglisten-Bildschirm ("Loserboard") dar.
 * @param navController Der NavController zur Steuerung der Navigation.
 * @param viewModel Der ViewModel, der die Ranglistendaten bereitstellt.
 */
@Composable
fun RankingsScreen(
    navController: NavController,
    viewModel: RankingsViewModel = viewModel(
        factory = RankingsViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    // Lädt die Rangliste, wenn der Bildschirm zum ersten Mal angezeigt wird.
    LaunchedEffect(Unit) {
        viewModel.loadRankings()
    }

    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.statusBarsPadding().height(16.dp))
                }

                // Header-Karte
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Loserboard",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Congratulations to our champions of failure.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Podium für die Top 3 Spieler
                if (uiState.players.isNotEmpty()) {
                    item {
                        val topThree = uiState.players.take(3)
                        TopThreePlayers(players = topThree)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Platzhalter, wenn nur ein Spieler existiert
                if (uiState.players.size <= 1) {
                    item {
                        SinglePlayerPlaceholder()
                    }
                } else {
                    // Liste für die restlichen Spieler (ab Platz 4)
                    val remainingPlayers = if (uiState.players.size > 3) uiState.players.subList(3, uiState.players.size) else emptyList()
                    itemsIndexed(remainingPlayers, key = { _, player -> player.id }) { index, player ->
                        PlayerListItem(
                            rank = index + 4,
                            player = player
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

/**
 * Zeigt eine Platzhalter-Karte an, wenn nur ein Spieler existiert.
 */
@Composable
fun SinglePlayerPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "Crown",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Reigning Embarrassment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "…until someone worse shows up.",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Stellt die Top-3-Spieler in einer Podiumsanordnung dar.
 */
@Composable
fun TopThreePlayers(players: List<Player>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LightPurple,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            if (players.size >= 2) {
                PodiumPlayer(player = players[1], rank = 2, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (players.isNotEmpty()) {
                PodiumPlayer(player = players[0], rank = 1, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (players.size >= 3) {
                PodiumPlayer(player = players[2], rank = 3, modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Stellt einen einzelnen Spieler auf dem Podium dar.
 */
@Composable
fun PodiumPlayer(player: Player, rank: Int, modifier: Modifier = Modifier) {
    val isFirstPlace = rank == 1
    val size = if (isFirstPlace) 100.dp else 80.dp
    val topPadding = if (isFirstPlace) 0.dp else 24.dp

    Column(
        modifier = modifier.padding(top = topPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            RankingsPlayerAvatar(
                size = size,
                imageUri = player.imageUri,
                modifier = Modifier.padding(top = 12.dp)
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> MaterialTheme.colorScheme.secondary
                            2 -> Color(0xFFA9A4E0)
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                    .border(2.dp, LightPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontWeight = FontWeight.Bold,
                    color = when (rank) {
                        1 -> MaterialTheme.colorScheme.onSecondary
                        2 -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onTertiary
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${player.totalShameScore}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Stellt ein Listenelement für einen Spieler ab Rang 4 dar.
 */
@Composable
fun PlayerListItem(rank: Int, player: Player) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            RankingsPlayerAvatar(size = 50.dp, imageUri = player.imageUri)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${player.totalShameScore}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Stellt den Avatar eines Spielers dar, mit Lade- und Fehlerzuständen.
 */
@Composable
fun RankingsPlayerAvatar(size: Dp, imageUri: String?, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = imageUri?.toUri(),
        contentDescription = "Player Avatar",
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = ContentScale.Crop,
        loading = { CircularProgressIndicator() },
        error = { Icon(Icons.Default.Person, contentDescription = "Placeholder") },
        success = { state ->
            Image(
                painter = state.painter,
                contentDescription = "Player Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}