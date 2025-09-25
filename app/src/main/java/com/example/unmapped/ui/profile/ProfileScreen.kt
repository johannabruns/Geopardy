package com.example.unmapped.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.unmapped.R
import com.example.unmapped.data.*
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.theme.DarkGray
import com.example.unmapped.ui.theme.LightPurple
import com.example.unmapped.ui.theme.MediumPurple
import java.io.File
import java.text.DecimalFormat
import java.util.*

/**
 * Eine Hilfsfunktion, die einer Badge-ID die entsprechenden farbigen und grauen Icon-Ressourcen zuordnet.
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

/**
 * Stellt den Profilbildschirm des Hauptspielers dar.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    val context = LocalContext.current
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val mainPlayer = uiState.mainPlayer

    var showEditDialog by remember { mutableStateOf(false) }
    var dialogTempName by remember { mutableStateOf("") }
    var dialogTempUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBadgeForDetail by remember { mutableStateOf<BadgeDefinition?>(null) }
    var showLockedBadgeDialog by remember { mutableStateOf(false) }
    var lockedBadgeQuote by remember { mutableStateOf("") }

    val lockedQuotes = remember {
        listOf(
            "You’re okay… for now.", "Potential for chaos: still untapped.", "The clown shoes are waiting for you.",
            "Not embarrassing enough yet, try harder.", "Stay humble, your flop era is loading.",
            "This badge is just waiting for your downfall.", "You can’t hide your mediocrity forever.",
            "Locked… but we both know it won’t stay that way.", "Oh, you thought you were safe?",
            "Don’t worry, humiliation comes to those who wait.", "Your shame arc hasn’t peaked yet.",
            "This badge knows your downfall is inevitable.", "Every bad guess brings us closer together.",
            "Somewhere out there, your failure is brewing.", "Like your sense of direction, this is still locked.",
            "The GPS of shame hasn’t rerouted you here… yet.", "The universe is just waiting for you to flop harder."
        )
    }

    /**
     * Kopiert eine Datei von einer temporären Quell-URI in den permanenten Speicher der App.
     */
    fun copyUriToPermanentStorage(sourceUri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val permanentDir = File(context.filesDir, "profile_images").apply { mkdirs() }
            val permanentFile = File(permanentDir, "${mainPlayer?.id ?: UUID.randomUUID()}.jpg")
            inputStream?.use { input -> permanentFile.outputStream().use { output -> input.copyTo(output) } }
            Uri.fromFile(permanentFile)
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error copying file", e)
            null
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { sourceUri: Uri? ->
        sourceUri?.let {
            val permanentUri = copyUriToPermanentStorage(it)
            dialogTempUri = permanentUri
        }
    }

    if (mainPlayer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(
            mainPlayerName = mainPlayer.name,
            imageUri = mainPlayer.imageUri?.toUri(),
            onSettingsClick = { navController.navigate(Screen.Settings.route) },
            onEditClick = {
                dialogTempName = mainPlayer.name
                dialogTempUri = mainPlayer.imageUri?.toUri()
                showEditDialog = true
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        BadgesSection(
            badgesWithProgress = uiState.badges,
            onBadgeClick = { definition, isUnlocked ->
                if (isUnlocked) {
                    selectedBadgeForDetail = definition
                } else {
                    lockedBadgeQuote = lockedQuotes.randomOrNull() ?: "Try harder."
                    showLockedBadgeDialog = true
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        StatisticsSection(stats = uiState.gameStats, totalShameScore = mainPlayer.totalShameScore)
        Spacer(modifier = Modifier.height(120.dp))
    }

    // Dialoge, die bei Bedarf angezeigt werden.
    if (showEditDialog) {
        EditProfileDialog(
            nameValue = dialogTempName, onNameChange = { dialogTempName = it },
            currentProfileUri = dialogTempUri, onDismiss = { showEditDialog = false },
            onSave = {
                profileViewModel.updateUserName(dialogTempName)
                profileViewModel.updateProfileImageUri(dialogTempUri)
                showEditDialog = false
            },
            onPickImage = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        )
    }

    selectedBadgeForDetail?.let { badge -> BadgeDetailDialog(badge = badge, onDismiss = { selectedBadgeForDetail = null }) }
    if (showLockedBadgeDialog) { LockedBadgeDialog(quote = lockedBadgeQuote, onDismiss = { showLockedBadgeDialog = false }) }
}

/**
 * Stellt den Header-Bereich des Profils dar.
 */
@Composable
fun ProfileHeader(mainPlayerName: String, imageUri: Uri?, onSettingsClick: () -> Unit, onEditClick: () -> Unit) {
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp),
            color = MediumPurple
        ) {}
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = imageUri ?: R.drawable.icon_gamer_black, contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentScale = ContentScale.Crop, error = painterResource(id = R.drawable.icon_gamer_black)
                )
                Surface(
                    modifier = Modifier.offset(x = 8.dp, y = 8.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    onClick = onEditClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier
                            .size(36.dp)
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = mainPlayerName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Stellt die Sektion "Hall of Shame" dar, die die Badges enthält.
 */
@Composable
fun BadgesSection(badgesWithProgress: Map<BadgeDefinition, BadgeProgress>, onBadgeClick: (BadgeDefinition, Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hall of Shame", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            HallOfShameBadgeList(badgesWithProgress, onBadgeClick)
        }
    }
}

/**
 * Stellt die scrollbare Gitteransicht der Badges dar.
 */
@Composable
fun HallOfShameBadgeList(badgesWithProgress: Map<BadgeDefinition, BadgeProgress>, onBadgeClick: (BadgeDefinition, Boolean) -> Unit) {
    val sortedBadges = badgesWithProgress.entries.toList().sortedBy { it.key.id }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(350.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = true
    ) {
        items(sortedBadges, key = { it.key.id }) { (definition, progress) ->
            BadgeItem(
                definition = definition, progress = progress,
                onClick = { onBadgeClick(definition, progress.currentProgress >= definition.requiredProgress) }
            )
        }
    }
}

/**
 * Stellt ein einzelnes Badge-Icon dar, inklusive Fortschrittsanzeige.
 */
@Composable
fun BadgeItem(definition: BadgeDefinition, progress: BadgeProgress, onClick: () -> Unit) {
    val isUnlocked = progress.currentProgress >= definition.requiredProgress
    val (colorRes, greyRes) = getBadgeIconRes(badgeId = definition.id)
    val iconResource = if (isUnlocked) colorRes else greyRes
    val backgroundColor = if (isUnlocked) LightPurple else DarkGray
    val progressFloat = (progress.currentProgress.toFloat() / definition.requiredProgress.toFloat()).coerceIn(0f, 1f)

    Box(contentAlignment = Alignment.Center, modifier = Modifier
        .aspectRatio(1f)
        .clickable(onClick = onClick)) {
        if (!isUnlocked && progress.currentProgress > 0) {
            CircularProgressIndicator(
                progress = 1f,
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 3.dp
            )
            CircularProgressIndicator(
                progress = progressFloat,
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
                Image(painter = painterResource(id = iconResource), contentDescription = definition.name, modifier = Modifier.fillMaxSize(0.8f))
            }
        }
    }
}

/**
 * Stellt die Sektion "Loser Metrics" dar, die die Spielstatistiken anzeigt.
 */
@Composable
fun StatisticsSection(stats: GameStats, totalShameScore: Int) {
    val df = remember { DecimalFormat("#,##0") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Loser Metrics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = df.format(totalShameScore), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = "Overall Amount of Failure", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatItem(title = "Total Games", value = (stats.totalRounds / 5).toString(), modifier = Modifier.weight(1f))
                    StatItem(title = "Average Miss", value = "${df.format(stats.avgDistanceKm)} km", modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatItem(title = "Least Embarrassing", value = "${df.format(stats.bestDistanceKm ?: 0.0)} km", modifier = Modifier.weight(1f))
                    StatItem(title = "Most Embarrassing", value = "${df.format(stats.worstDistanceKm ?: 0.0)} km", modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatItem(title = "Quick Disasters\n(under 30s)", value = stats.fastGuesses.toString(), modifier = Modifier.weight(1f))
                    StatItem(title = "Timer Terrorist\n(full 2min)", value = stats.slowGuesses.toString(), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Stellt ein einzelnes Statistik-Element dar.
 */
@Composable
fun StatItem(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/**
 * Stellt einen Dialog zum Bearbeiten des Profils (Name und Bild) dar.
 */
@Composable
fun EditProfileDialog(nameValue: String, onNameChange: (String) -> Unit, currentProfileUri: Uri?, onDismiss: () -> Unit, onSave: () -> Unit, onPickImage: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Edit Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onPickImage),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentProfileUri ?: R.drawable.icon_gamer_black, contentDescription = "Profile Picture Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape), contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.icon_gamer_black)
                    )
                    Box(modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Change Picture", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = nameValue, onValueChange = onNameChange, label = { Text("Username") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave) { Text("Save") }
                }
            }
        }
    }
}

/**
 * Stellt einen Dialog dar, der die Details zu einem freigeschalteten Badge anzeigt.
 */
@Composable
fun BadgeDetailDialog(badge: BadgeDefinition, onDismiss: () -> Unit) {
    val (colorRes, _) = getBadgeIconRes(badgeId = badge.id)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp), shape = RoundedCornerShape(24.dp)) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MediumPurple)
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = colorRes), contentDescription = badge.name,
                        modifier = Modifier.size(180.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = badge.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = badge.description, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("whatever") }
                }
            }
        }
    }
}

/**
 * Stellt einen Dialog dar, der angezeigt wird, wenn auf ein noch nicht freigeschaltetes Badge geklickt wird.
 */
@Composable
fun LockedBadgeDialog(quote: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(quote, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("guess I'll wait.") }
            }
        }
    }
}