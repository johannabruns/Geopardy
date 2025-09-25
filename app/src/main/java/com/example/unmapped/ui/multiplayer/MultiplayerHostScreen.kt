package com.example.unmapped.ui.multiplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.example.unmapped.ui.components.GameLoadingScreen
import com.example.unmapped.R

/**
 * Dient als zentraler Router für den Multiplayer-Modus.
 * Diese Composable-Funktion beobachtet die `GamePhase` aus dem [MultiplayerViewModel]
 * und zeigt basierend darauf den entsprechenden Bildschirm an (z.B. Lade-, Spiel- oder Ergebnisbildschirm).
 *
 * @param navController Der [NavHostController] für die Navigation.
 * @param viewModel Der geteilte [MultiplayerViewModel], der den Zustand des gesamten Spiels verwaltet.
 */
@Composable
fun MultiplayerHostScreen(
    navController: NavHostController,
    viewModel: MultiplayerViewModel
) {
    val gameState by viewModel.gameState.collectAsState()

    // Die `when`-Anweisung leitet zur richtigen UI-Komponente für die aktuelle Spielphase weiter.
    when (gameState.phase) {
        GamePhase.LOADING -> {
            GameLoadingScreen(backgroundImageRes = R.drawable.card_multi) {}
        }
        GamePhase.GAME_START, GamePhase.ROUND_TRANSITION -> {
            MultiplayerTransitionScreen(viewModel = viewModel)
        }
        GamePhase.PLAYING -> {
            MultiplayerGameScreen(navController = navController, viewModel = viewModel)
        }
        GamePhase.ROUND_RESULT -> {
            MultiplayerResultScreen(viewModel = viewModel)
        }
        GamePhase.GAME_OVER -> {
            MultiplayerEndScreen(navController = navController, viewModel = viewModel)
        }
    }
}