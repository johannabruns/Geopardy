package com.example.unmapped.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.unmapped.data.Player
import com.example.unmapped.ui.challenge.*
import com.example.unmapped.ui.createprofile.CreateProfileScreen
import com.example.unmapped.ui.home.HomeScreen
import com.example.unmapped.ui.multiplayer.*
import com.example.unmapped.ui.profile.ProfileScreen
import com.example.unmapped.ui.rankings.RankingsScreen
import com.example.unmapped.ui.recommended.*
import com.example.unmapped.ui.settings.SettingsScreen
import com.example.unmapped.ui.singleplayer.*
import com.example.unmapped.ui.start.StartScreen

/**
 * Definiert alle Navigationsrouten der App als type-safe Objekte.
 * Dies verhindert Tippfehler bei der Angabe von Routen-Strings.
 */
sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Home : Screen("home")
    object Rankings : Screen("rankings")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object GameGraph : Screen("game_graph")
    object GameLoading : Screen("game_loading")
    object GamePlay : Screen("game_play")
    object GameResult : Screen("game_result")
    object GameEnd : Screen("game_end")
    object MultiPlayerSetup : Screen("multiplayer_setup")
    object MultiplayerGameGraph : Screen("multiplayer_game_graph")
    // KORREKTUR: Wir brauchen nur noch einen Screen für den gesamten Multiplayer-Flow
    object MultiplayerHost : Screen("multiplayer_host")
    object ChallengeGraph : Screen("challenge_graph")
    object ChallengeLoading : Screen("challenge_loading")
    object ChallengePlay : Screen("challenge_play")
    object ChallengeResult : Screen("challenge_result")
    object ChallengeEnd : Screen("challenge_end")
    object RecommendedMapGraph : Screen("recommended_map_graph")
    object RecommendedMapLoading : Screen("recommended_map_loading")
    object RecommendedMapPlay : Screen("recommended_map_play")
    object RecommendedMapResult : Screen("recommended_map_result")
    object RecommendedMapEnd : Screen("recommended_map_end")
    object CreateProfile : Screen("create_profile")
}

/**
 * Der zentrale Navigations-Host, der alle Screens und Navigationsgraphen der App verwaltet.
 *
 * @param navController Der Controller, der die Navigation steuert.
 * @param startDestination Die Route des ersten Screens, der angezeigt werden soll.
 * @param modifier Ein Modifier für das Layout.
 */
@Composable
fun AppNavHost(navController: NavHostController, startDestination: String, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Definition der Top-Level-Screens
        composable(Screen.Start.route) { StartScreen(navController) }
        composable(Screen.CreateProfile.route) { CreateProfileScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Rankings.route) { RankingsScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.MultiPlayerSetup.route) { MultiPlayerSetupScreen(navController) }

        // Einbindung der verschachtelten Navigationsgraphen für Spielflüsse
        singlePlayerGraph(navController)
        multiplayerGraph(navController)
        challengeGraph(navController)
        recommendedMapGraph(navController)
    }
}

/**
 * Definiert den Navigationsgraphen für den gesamten Multiplayer-Modus.
 * Dieser Ansatz nutzt einen einzigen "Host"-Screen und ein geteiltes ViewModel,
 * um den Zustand über den gesamten Spielfluss (Laden, Spielen, Ergebnis) zu verwalten.
 */
private fun NavGraphBuilder.multiplayerGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.MultiplayerHost.route,
        route = Screen.MultiplayerGameGraph.route
    ) {
        composable(Screen.MultiplayerHost.route) { backStackEntry ->
            // Holt den übergeordneten NavGraph-Eintrag, um den Geltungsbereich des ViewModels festzulegen.
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.MultiplayerGameGraph.route)
            }
            // Erstellt den ViewModel, der an den NavGraph gebunden ist.
            // Er überlebt somit, solange man sich im Multiplayer-Flow befindet.
            val gameViewModel: MultiplayerViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = MultiplayerViewModelFactory(
                    LocalContext.current.applicationContext as Application
                )
            )

            // Holt die Spielerliste, die vom Setup-Screen über den SavedStateHandle übergeben wurde.
            val players = navController.previousBackStackEntry
                ?.savedStateHandle?.get<List<Player>>("players")

            // Startet das Spiel im ViewModel, aber nur ein einziges Mal, wenn die Spielerliste ankommt.
            LaunchedEffect(players) {
                if (players != null && gameViewModel.gameState.value.players.isEmpty()) {
                    gameViewModel.setupGame(players)
                }
            }

            // Zeigt den Host-Screen an, der die Logik zur Anzeige des richtigen
            // Inhalts (Laden, Spiel, Ergebnis) basierend auf dem ViewModel-Zustand enthält.
            MultiplayerHostScreen(navController = navController, viewModel = gameViewModel)
        }
    }
}


/**
 * Definiert den Navigationsgraphen für den Single-Player-Modus.
 * Jeder Screen (Laden, Spielen, etc.) ist eine eigene Route innerhalb dieses Graphen.
 */
private fun NavGraphBuilder.singlePlayerGraph(navController: NavHostController) {
    navigation(startDestination = Screen.GameLoading.route, route = Screen.GameGraph.route) {
        composable(Screen.GameLoading.route) {
            val gameViewModel = it.sharedViewModel<GameViewModel>(navController)
            SinglePlayerLoadingScreen(navController, gameViewModel)
        }
        composable(Screen.GamePlay.route) {
            val gameViewModel = it.sharedViewModel<GameViewModel>(navController)
            SinglePlayerScreen(navController, gameViewModel)
        }
        composable(Screen.GameResult.route) {
            val gameViewModel = it.sharedViewModel<GameViewModel>(navController)
            SinglePlayerResultScreen(navController, gameViewModel)
        }
        composable(Screen.GameEnd.route) {
            val gameViewModel = it.sharedViewModel<GameViewModel>(navController)
            SinglePlayerEndOfGameScreen(navController, gameViewModel)
        }
    }
}

/**
 * Definiert den Navigationsgraphen für den Challenge-Modus.
 * Jeder Screen (Laden, Spielen, etc.) ist eine eigene Route innerhalb dieses Graphen.
 */
private fun NavGraphBuilder.challengeGraph(navController: NavHostController) {
    navigation(startDestination = Screen.ChallengeLoading.route, route = Screen.ChallengeGraph.route) {
        composable(Screen.ChallengeLoading.route) {
            val challengeViewModel = it.sharedViewModel<ChallengeViewModel>(navController)
            ChallengeLoadingScreen(navController, challengeViewModel)
        }
        composable(Screen.ChallengePlay.route) {
            val challengeViewModel = it.sharedViewModel<ChallengeViewModel>(navController)
            ChallengeScreen(navController, challengeViewModel)
        }
        composable(Screen.ChallengeResult.route) {
            val challengeViewModel = it.sharedViewModel<ChallengeViewModel>(navController)
            ChallengeResultScreen(navController, challengeViewModel)
        }
        composable(Screen.ChallengeEnd.route) {
            val challengeViewModel = it.sharedViewModel<ChallengeViewModel>(navController)
            ChallengeEndOfGameScreen(navController, challengeViewModel)
        }
    }
}

/**
 * Definiert den Navigationsgraphen für den Recommended-Maps-Modus.
 * Jeder Screen (Laden, Spielen, etc.) ist eine eigene Route innerhalb dieses Graphen.
 */
private fun NavGraphBuilder.recommendedMapGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.RecommendedMapLoading.route,
        route = "${Screen.RecommendedMapGraph.route}/{mapId}",
        arguments = listOf(navArgument("mapId") { type = NavType.StringType })
    ) {
        val graphRoute = "${Screen.RecommendedMapGraph.route}/{mapId}"

        composable(Screen.RecommendedMapLoading.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(graphRoute) }
            val mapId = parentEntry.arguments?.getString("mapId") ?: ""
            val viewModel: RecommendedViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = RecommendedViewModelFactory(LocalContext.current.applicationContext as Application, mapId)
            )
            RecommendedLoadingScreen(navController, viewModel)
        }
        composable(Screen.RecommendedMapPlay.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(graphRoute) }
            val viewModel: RecommendedViewModel = viewModel(viewModelStoreOwner = parentEntry)
            RecommendedScreen(navController, viewModel)
        }
        composable(Screen.RecommendedMapResult.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(graphRoute) }
            val viewModel: RecommendedViewModel = viewModel(viewModelStoreOwner = parentEntry)
            RecommendedResultScreen(navController, viewModel)
        }
        composable(Screen.RecommendedMapEnd.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(graphRoute) }
            val viewModel: RecommendedViewModel = viewModel(viewModelStoreOwner = parentEntry)
            RecommendedEndOfGameScreen(navController, viewModel)
        }
    }
}

/**
 * Eine nützliche Erweiterungsfunktion, um ein ViewModel zu erhalten, das an den
 * übergeordneten Navigationsgraphen gebunden ist. Dies ist der Schlüssel zum Teilen
 * von ViewModels zwischen mehreren Screens eines Spielflusses.
 *
 * @param navController Der NavHostController.
 * @return Die Instanz des geteilten ViewModels.
 */
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavHostController,
    mapId: String? = null
): T {
    // Ermittelt die Route des übergeordneten Graphen (z.B. "game_graph").
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    // Holt den BackStackEntry dieses Graphen.
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    val factory = when (T::class) {
        GameViewModel::class -> GameViewModelFactory(LocalContext.current.applicationContext as Application)
        else -> null
    }
    // Erstellt und gibt den ViewModel zurück, der an diesen Graphen gebunden ist.
    return if (factory != null) {
        viewModel(parentEntry, factory = factory)
    } else {
        viewModel(parentEntry)
    }
}