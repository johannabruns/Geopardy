package com.example.unmapped.ui.home

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.unmapped.R
import com.example.unmapped.data.Player
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.theme.UnmappedTheme
import com.example.unmapped.ui.theme.LightPurple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Datenklasse für die Darstellung einer Karte in der "Recommended Maps"-Sektion.
 */
data class MapCardData(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageResId: Int
)

/**
 * Datenklasse für die Darstellung einer Karte in der "Game Modes"-Sektion.
 */
data class GameModeData(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageResId: Int
)

/**
 * Stellt den Hauptbildschirm der App dar.
 * Zeigt einen Header mit Spielerinfos, eine Auswahl an Spielmodi und empfohlene Karten an.
 *
 * @param navController Der [NavHostController] zur Steuerung der Navigation.
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 * @param homeViewModel Der [HomeViewModel], der die Daten für diesen Bildschirm bereitstellt.
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    // Dieser Effekt sorgt dafür, dass die Daten immer dann aktualisiert werden,
    // wenn der Benutzer zum Home-Bildschirm zurückkehrt (z.B. nach einem Spiel).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

        // Abstandshalter, der den Inhalt unter die System-Statusleiste schiebt.
        Spacer(modifier = Modifier.statusBarsPadding().height(32.dp))

        // Zeigt den Header an, sobald die Spielerdaten geladen sind.
        if (uiState.mainPlayer != null) {
            HeaderSection(
                playerName = uiState.mainPlayer!!.name,
                shameScore = uiState.totalShameScore.toString(),
                imageUri = uiState.mainPlayer!!.imageUri?.toUri()
            )
        } else {
            // Zeigt eine Ladeanzeige, während die Daten abgerufen werden.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Sektion für die Spielmodi.
        Text(
            text = "game modes",
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            val gameModes = listOf(
                GameModeData("SinglePlayer", "play solo", "flop alone.", R.drawable.card_single),
                GameModeData("MultiPlayer", "with friends", "roast each other.", R.drawable.card_multi),
                GameModeData("Challenge", "challenges", "you're delusional.", R.drawable.card_challenge)
            )
            items(gameModes) { gameMode ->
                val route = when (gameMode.id) {
                    "SinglePlayer" -> Screen.GameGraph.route
                    "MultiPlayer" -> Screen.MultiPlayerSetup.route
                    else -> Screen.ChallengeGraph.route
                }
                GameModeCard(
                    title = gameMode.title,
                    subtitle = gameMode.subtitle,
                    imageResId = gameMode.imageResId,
                    onClick = { navController.navigate(route) }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Sektion für die empfohlenen Karten.
        Text(
            text = "recommended maps",
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            val recommendedMaps = listOf(
                MapCardData("tourist_traps", "tourist traps", "mainstream misery", R.drawable.card_tourist),
                MapCardData("pop_culture_hotspots", "pop culture", "as seen on tv", R.drawable.card_popculture),
                MapCardData("cancelled_destinations", "cancelled", "yikes", R.drawable.card_cancelled),
                MapCardData("conspiracy_core", "conspiracy core", "questionable af", R.drawable.card_conspiracy)
            )
            items(recommendedMaps) { mapData ->
                RecommendedMapCard(
                    title = mapData.title,
                    subtitle = mapData.subtitle,
                    imageResId = mapData.imageResId,
                    onClick = {
                        // Navigiert zur passenden empfohlenen Karte und übergibt deren ID als Argument.
                        navController.navigate("${Screen.RecommendedMapGraph.route}/${mapData.id}")
                    }
                )
            }
        }
        // Abstand am Ende, damit der Inhalt nicht von der schwebenden Navigationsleiste verdeckt wird.
        Spacer(modifier = Modifier.height(120.dp))
    }
}

/**
 * Stellt den Header-Bereich auf dem Home-Bildschirm dar.
 *
 * @param playerName Der Name des Spielers.
 * @param shameScore Der Punktestand des Spielers.
 * @param imageUri Die URI des Profilbilds des Spielers.
 */
@Composable
private fun HeaderSection(playerName: String, shameScore: String, imageUri: Uri?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = LightPurple,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUri ?: R.drawable.icon_gamer_black,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.icon_gamer_black)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Hi, $playerName!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Zeigt unterschiedliche Texte an, je nachdem ob der Spieler bereits Punkte hat.
                if (shameScore == "0") {
                    Text(
                        text = "Your mediocre skills have not earned you any points. Yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                } else {
                    // Verwendet `buildAnnotatedString`, um den Punktestand farblich hervorzuheben.
                    Text(
                        text = buildAnnotatedString {
                            append("Your mediocre skills earned you ")
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                append("$shameScore points")
                            }
                            append(". That's tragic.")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Stellt eine klickbare Karte für einen Spielmodus dar.
 *
 * @param title Der Titel des Spielmodus.
 * @param subtitle Der Untertitel des Spielmodus.
 * @param imageResId Die Ressourcen-ID des Bildes für die Karte.
 * @param onClick Die Aktion, die beim Klick auf die Karte ausgeführt wird.
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameModeCard(
    title: String,
    subtitle: String,
    imageResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Stellt eine klickbare Karte für eine empfohlene Karte dar.
 *
 * @param title Der Titel der Karte.
 * @param subtitle Der Untertitel der Karte.
 * @param imageResId Die Ressourcen-ID des Bildes für die Karte.
 * @param onClick Die Aktion, die beim Klick auf die Karte ausgeführt wird.
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedMapCard(
    title: String,
    subtitle: String,
    imageResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Eine Vorschau-Funktion für die Entwicklung in Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    UnmappedTheme {
        HomeScreen(navController = rememberNavController())
    }
}