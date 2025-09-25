package com.example.unmapped.ui.recommended

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.unmapped.R
import com.example.unmapped.data.*
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.theme.DarkGray
import com.example.unmapped.ui.theme.LightPurple
import com.example.unmapped.ui.theme.MediumPurple
import java.text.DecimalFormat

/**
 * Hilfs-Datenklasse, um das Ergebnis der Badge-Analyse zu speichern.
 */
private data class BadgeUpdateResult(
    val unlocked: List<Pair<BadgeDefinition, BadgeProgress>> = emptyList(),
    val progressed: List<Pair<BadgeDefinition, BadgeProgress>> = emptyList()
)

/**
 * Stellt den Endbildschirm nach Abschluss einer "Recommended Map" dar.
 */
@Composable
fun RecommendedEndOfGameScreen(
    navController: NavHostController,
    viewModel: RecommendedViewModel
) {
    val gameState by viewModel.gameState.collectAsState()
    val totalScore = gameState.gameRounds.sumOf { it.result?.shameScore ?: 0 }
    val context = LocalContext.current
    var badgeUpdates by remember { mutableStateOf<BadgeUpdateResult?>(null) }
    var showBadgeInfoDialog by remember { mutableStateOf(false) }

    // Führt die Logik nach Spielende aus.
    LaunchedEffect(key1 = gameState.isGameFinished) {
        if (gameState.isGameFinished) {
            // 1. Speichert den Gesamtpunktestand des Spielers.
            val playerRepository = PlayerRepository(context)
            val profilePrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            val mainPlayerId = profilePrefs.getString("main_player_id", null)
            if (mainPlayerId != null) {
                playerRepository.addScoreToPlayer(mainPlayerId, totalScore)
            }

            // 2. Analysiert und speichert den allgemeinen Badge-Fortschritt.
            val roundResults = gameState.gameRounds.mapNotNull { it.result }
            if (roundResults.isNotEmpty()) {
                val badgeRepository = BadgeRepository(context)
                val allBadgeDefs = allBadges.associateBy { it.id }
                val oldProgressMap = badgeRepository.getBadgeProgress().associateBy { it.badgeId }
                BadgeManager.processGameResults(context, roundResults)
                val newProgressList = badgeRepository.getBadgeProgress()
                val newlyUnlocked = mutableListOf<Pair<BadgeDefinition, BadgeProgress>>()
                val newlyProgressed = mutableListOf<Pair<BadgeDefinition, BadgeProgress>>()
                newProgressList.forEach { newProgress ->
                    val oldProg = oldProgressMap[newProgress.badgeId]
                    val definition = allBadgeDefs[newProgress.badgeId]
                    if (definition != null && oldProg != null) {
                        val wasUnlocked = oldProg.currentProgress >= definition.requiredProgress
                        val isUnlocked = newProgress.currentProgress >= definition.requiredProgress
                        if (isUnlocked && !wasUnlocked) {
                            newlyUnlocked.add(definition to newProgress)
                        } else if (!isUnlocked && newProgress.currentProgress > oldProg.currentProgress) {
                            newlyProgressed.add(definition to newProgress)
                        }
                    }
                }
                badgeUpdates = BadgeUpdateResult(unlocked = newlyUnlocked, progressed = newlyProgressed)
            }

            // 3. Spezifische Logik für diesen Modus: Markiert sehr gute Schätzungen als "gespielt".
            val locationRepository = LocationRepository(context)
            val correctlyGuessedLocations = gameState.gameRounds
                .mapNotNull { it.result }
                .filter { it.distanceInMeters <= 500 } // Schwellenwert für eine korrekte Schätzung.
                .map { it.actualLocation }
            if (correctlyGuessedLocations.isNotEmpty()) {
                locationRepository.markLocationsAsPlayed(viewModel.mapId, correctlyGuessedLocations)
            }
        }
    }

    if (showBadgeInfoDialog) {
        BadgeInfoDialog(onDismiss = { showBadgeInfoDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EndOfGameHeader(totalScore = totalScore, title = "You're done.")

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        gameState.gameRounds.forEachIndexed { index, round ->
                            RoundSummaryRow(
                                roundNumber = index + 1,
                                distanceKm = (round.result?.distanceInMeters ?: 0.0) / 1000,
                                score = round.result?.shameScore ?: 0
                            )
                            if (index < gameState.gameRounds.lastIndex) {
                                Divider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            badgeUpdates?.let { updates ->
                if (updates.unlocked.isNotEmpty() || updates.progressed.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        ProgressedBadgesSection(
                            unlockedBadges = updates.unlocked,
                            progressedBadges = updates.progressed,
                            onBadgeClick = { showBadgeInfoDialog = true }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    navController.navigate("${Screen.RecommendedMapGraph.route}/${viewModel.mapId}") {
                        popUpTo(Screen.RecommendedMapGraph.route) { inclusive = true }
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
                        popUpTo(Screen.RecommendedMapGraph.route) { inclusive = true }
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
 * Stellt die Sektion dar, die neu freigeschaltete oder verbesserte Badges anzeigt.
 */
@Composable
private fun ProgressedBadgesSection(
    unlockedBadges: List<Pair<BadgeDefinition, BadgeProgress>>,
    progressedBadges: List<Pair<BadgeDefinition, BadgeProgress>>,
    onBadgeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Hall Of Shame",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(unlockedBadges, key = { it.first.id }) { (definition, progress) ->
                    BadgeItem(
                        definition = definition,
                        progress = progress,
                        isNewlyUnlocked = true,
                        onClick = onBadgeClick
                    )
                }
                items(progressedBadges, key = { it.first.id }) { (definition, progress) ->
                    BadgeItem(
                        definition = definition,
                        progress = progress,
                        onClick = onBadgeClick
                    )
                }
            }
        }
    }
}

/**
 * Stellt ein einzelnes Badge-Icon dar, inklusive Fortschrittsanzeige.
 */
@Composable
private fun BadgeItem(
    definition: BadgeDefinition,
    progress: BadgeProgress,
    onClick: () -> Unit,
    isNewlyUnlocked: Boolean = false
) {
    val isUnlocked = progress.currentProgress >= definition.requiredProgress
    val (colorRes, greyRes) = getBadgeIconRes(badgeId = definition.id)
    val iconResource = if (isUnlocked) colorRes else greyRes
    val backgroundColor = if (isUnlocked) LightPurple else DarkGray
    val progressFloat = (progress.currentProgress.toFloat() / definition.requiredProgress.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progressFloat, label = "BadgeProgressAnimation")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            if (!isUnlocked && progress.currentProgress > 0) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 3.dp
                )
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    strokeCap = StrokeCap.Round
                )
            }
            Surface(
                modifier = Modifier.fillMaxSize(0.85f),
                shape = CircleShape,
                color = backgroundColor,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = iconResource),
                        contentDescription = definition.name,
                        modifier = Modifier.fillMaxSize(0.8f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isNewlyUnlocked) "Unlocked!" else "${progress.currentProgress}/${definition.requiredProgress}",
            style = MaterialTheme.typography.bodySmall,
            color = if (isNewlyUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isNewlyUnlocked) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Zeigt einen Informationsdialog über die "Hall of Shame" an.
 */
@Composable
private fun BadgeInfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MediumPurple,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.MilitaryTech,
                            contentDescription = "Hall of Shame",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hall Of Shame",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your badges are on display in your Hall of Shame. Go to your profile and have a look at your disappointing achievements.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("urgh, fine.", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/**
 * Stellt den Header für den Endbildschirm dar.
 */
@Composable
private fun EndOfGameHeader(totalScore: Int, title: String) {
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
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )) {
                        append("$totalScore points")
                    }
                    append(" added to your pile of shame.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Stellt eine einzelne Zeile in der Rundenübersicht dar.
 */
@Composable
private fun RoundSummaryRow(roundNumber: Int, distanceKm: Double, score: Int) {
    val df = remember { DecimalFormat("#,##0") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Round $roundNumber",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${df.format(distanceKm)} km",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "+$score",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Eine Hilfsfunktion, die einer Badge-ID die entsprechenden Icon-Ressourcen zuordnet.
 */
@Composable
private fun getBadgeIconRes(badgeId: String): Pair<Int, Int> {
    return when (badgeId) {
        "SMART_ASS" -> R.drawable.smartass_color to R.drawable.smartass_grey
        "CRITICAL_OVERTHINKER" -> R.drawable.criticaloverthinker_color to R.drawable.criticaloverthinker_grey
        "US_AMERICAN" -> R.drawable.usamerican_color to R.drawable.usamerican_grey
        "CONSISTENTLY_MID" -> R.drawable.consistentlymid_color to R.drawable.consistentlymid_grey
        "FLAT_EARTHER" -> R.drawable.flatearther_color to R.drawable.flatearther_grey
        "LOST_TOURIST" -> R.drawable.losttourist_color to R.drawable.losttourist_grey
        "NATIONAL_EMBARRASSMENT" -> R.drawable.nationalembarrasment_color to R.drawable.nationalembarrasment_grey
        "EUROCENTRIC_MUCH" -> R.drawable.eurocentricmuch_color to R.drawable.eurocentricmuch_grey
        "CHRONICALLY_WRONG" -> R.drawable.chronicallywrong_color to R.drawable.chronicallywrong_grey
        "GEOGRAPHY_DROPOUT" -> R.drawable.geographydropout_color to R.drawable.geographydropout_grey
        "CULTURAL_MENACE" -> R.drawable.culturalmenace_color to R.drawable.culturalmenace_grey
        "COLUMBUS" -> R.drawable.columbus_color to R.drawable.columbus_grey
        "GLOBAL_MENACE" -> R.drawable.globalmenace_color to R.drawable.globalmenace_grey
        "BARE_MINIMUM" -> R.drawable.bareminimum_color to R.drawable.bareminimum_grey
        "LATITUDE_LOSER" -> R.drawable.latitudeloser_color to R.drawable.latitudeloser_grey
        "LONGITUDE_LOSER" -> R.drawable.longtitudeloser_color to R.drawable.longtitudeloser_grey
        "CONTINENTAL_DRIFT" -> R.drawable.continentaldrift_color to R.drawable.continentaldrift_grey
        "TOURIST_TRAPS_MASTER" -> R.drawable.touristtraps_color to R.drawable.touristtraps_grey
        "POP_CULTURE_MASTER" -> R.drawable.popculturehotspots_color to R.drawable.popculturehotspots_grey
        "CANCELLED_DESTINATIONS_MASTER" -> R.drawable.cancelleddestinations_color to R.drawable.cancelleddestinations_grey
        "CONSPIRACY_CORE_MASTER" -> R.drawable.conspiracycore_color to R.drawable.conspiracycore_grey
        else -> R.drawable.ic_launcher_foreground to R.drawable.ic_launcher_background
    }
}