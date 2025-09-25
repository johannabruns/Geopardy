package com.example.unmapped.ui.singleplayer

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.example.unmapped.data.calculateShameScore
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.components.GameBottomButton
import com.example.unmapped.ui.components.GameExitButton
import com.example.unmapped.ui.components.GameInfoCard
import com.example.unmapped.ui.guess.GuessOverlay
import com.example.unmapped.utils.LocationUtils
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.model.LatLng

/**
 * Stellt den Haupt-Gameplay-Bildschirm für den Einzelspielermodus dar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinglePlayerScreen(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    val gameState by viewModel.gameState.collectAsState()
    var showGuessDialog by remember { mutableStateOf(false) }
    var guessPosition by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(gameState.forceGuess) { if (gameState.forceGuess) { showGuessDialog = true } }

    if (gameState.gameRounds.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val currentRound = gameState.gameRounds[gameState.currentRoundIndex]

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameExitButton(onClick = { viewModel.onExitAttempt() })
                GameInfoCard {
                    Text(
                        text = gameState.formattedTime,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                GameInfoCard {
                    Text(
                        text = "Disaster ${gameState.currentRoundIndex + 1} of 5",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                GameBottomButton(onClick = {
                    viewModel.pauseTimer()
                    showGuessDialog = true
                })
            }
        }
    ) { scaffoldPadding ->
        GoogleStreetView(
            modifier = Modifier.fillMaxSize(),
            startCoordinates = currentRound.targetLocation,
            isNavigationEnabled = gameState.isMovementAllowed
        )

        if (gameState.showExitDialog) {
            ExitConfirmationDialog(
                onConfirm = {
                    viewModel.onExitConfirmed()
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.GameGraph.route) { inclusive = true } }
                },
                onDismiss = { viewModel.onExitDismissed() }
            )
        }
    }

    if (showGuessDialog) {
        GuessOverlay(
            markerPosition = guessPosition,
            onMapClick = { newPosition -> guessPosition = newPosition },
            onDismissRequest = {
                showGuessDialog = false
                guessPosition = null
                viewModel.resumeTimer()
            },
            onGuessConfirmed = { confirmedPosition ->
                showGuessDialog = false
                val distanceInMeters = LocationUtils.calculateDistanceInMeters(currentRound.targetLocation, confirmedPosition)
                val shameScore = calculateShameScore(distanceInMeters)
                viewModel.submitGuess(
                    guess = confirmedPosition,
                    distance = distanceInMeters,
                    score = shameScore,
                )
                guessPosition = null
                navController.navigate(Screen.GameResult.route)
            },
            isDismissible = !gameState.forceGuess
        )
    }
}

/**
 * Zeigt einen Bestätigungsdialog an, wenn der Spieler versucht, das Spiel zu verlassen.
 */
@Composable
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave Game?") },
        text = { Text("Your current progress in this round will be lost. Are you sure?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Leave") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Continue") } }
    )
}

/**
 * Eine Composable-Funktion, die eine Google Street View-Panoramaansicht in Jetpack Compose einbettet.
 * Sie kümmert sich um die korrekte Handhabung des Lebenszyklus der zugrunde liegenden Android View.
 *
 * @param modifier Ein Modifier zur Anpassung des Layouts.
 * @param startCoordinates Die initialen Koordinaten, die in Street View angezeigt werden sollen.
 * @param isNavigationEnabled Legt fest, ob sich der Benutzer in der Street View-Ansicht bewegen kann.
 */
@Composable
fun GoogleStreetView(modifier: Modifier = Modifier, startCoordinates: LatLng, isNavigationEnabled: Boolean) {
    val context = LocalContext.current
    // rememberSaveable stellt sicher, dass der Zustand von Street View (z.B. die Kameraposition)
    // bei Konfigurationsänderungen wie einer Bildschirmdrehung erhalten bleibt.
    val streetViewBundle = rememberSaveable { Bundle() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val streetView = remember { StreetViewPanoramaView(context).apply { onCreate(streetViewBundle) } }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> streetView.onStart()
                Lifecycle.Event.ON_RESUME -> streetView.onResume()
                Lifecycle.Event.ON_PAUSE -> {
                    streetView.onPause()
                    streetView.onSaveInstanceState(streetViewBundle)
                }
                Lifecycle.Event.ON_STOP -> streetView.onStop()
                Lifecycle.Event.ON_DESTROY -> streetView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // AndroidView ist die Brücke zwischen dem alten View-System und Jetpack Compose.
    AndroidView(
        factory = { streetView },
        modifier = modifier,
        update = { view ->
            view.getStreetViewPanoramaAsync { panorama ->
                panorama.isStreetNamesEnabled = false
                panorama.isUserNavigationEnabled = isNavigationEnabled
                panorama.isZoomGesturesEnabled = true
                panorama.isPanningGesturesEnabled = true
                panorama.setPosition(startCoordinates, 1000)
            }
        }
    )
}