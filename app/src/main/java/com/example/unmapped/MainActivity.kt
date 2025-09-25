package com.example.unmapped

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.unmapped.navigation.AppNavHost
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.MainViewModel
import com.example.unmapped.ui.StartDestinationState
import com.example.unmapped.ui.components.BottomNavBar
import com.example.unmapped.ui.components.NavBarViewModel
import com.example.unmapped.ui.components.NavBarViewModelFactory
import com.example.unmapped.ui.theme.UnmappedTheme
import com.example.unmapped.data.SettingsManager

/**
 * Die einzige Activity der App. Sie dient als Container für die gesamte Jetpack Compose UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Aktiviert den Edge-to-Edge-Modus für ein modernes, rahmenloses App-Layout.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { true },
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { true }
        )
        super.onCreate(savedInstanceState)

        setContent {
            // Holt den Dark-Mode-Status aus dem SettingsManager als Flow.
            val settingsManager = remember { SettingsManager(applicationContext) }
            val isDarkMode by settingsManager.isDarkMode.collectAsState(initial = false)

            // Wendet das App-Theme an und übergibt den dynamischen Dark-Mode-Status.
            UnmappedTheme(darkTheme = isDarkMode) {
            UnmappedTheme(darkTheme = isDarkMode) { // Übergeben Sie den Status an das Theme
                val mainViewModel: MainViewModel = viewModel()
                val startState by mainViewModel.startDestinationState.collectAsStateWithLifecycle()

                Surface(modifier = Modifier.fillMaxSize()) {
                }
                // Zeigt je nach Ladezustand entweder einen Ladekreis oder die Haupt-App an.
                when (startState) {
                        is StartDestinationState.Loading -> {
                            // Ladeanzeige, während der Start-Screen ermittelt wird.
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is StartDestinationState.Start -> {
                            // Haupt-App-Struktur, wenn der Start-Screen feststeht.
                            MainAppScaffold(startDestination = Screen.Start.route)
                        }
                        else -> {
                            // Fallback-Ladeanzeige.
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Das Haupt-Layout der App, das den NavHost und die schwebende Bottom Navigation Bar enthält.
 *
 * @param startDestination Die Route des Start-Screens.
 */
@Composable
private fun MainAppScaffold(startDestination: String) {
    val navController = rememberNavController()
    // Beobachtet den aktuellen BackStack, um die aktuelle Route zu ermitteln.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navBarViewModel: NavBarViewModel = viewModel(factory = NavBarViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
    val navBarState by navBarViewModel.uiState.collectAsStateWithLifecycle()

    // Dieser Effekt wird immer dann ausgeführt, wenn sich die 'currentRoute' ändert.
    LaunchedEffect(currentRoute) {
        // Prüft, ob die neue Route eine ist, auf der die Navigationsleiste angezeigt wird.
        if (currentRoute in listOf(Screen.Home.route, Screen.Rankings.route, Screen.Profile.route)) {
            // Wenn ja, werden die Daten für die Leiste (Profilbild, Name) neu geladen.
            navBarViewModel.refreshData()
        }
    }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { scaffoldOriginalPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Der NavHost füllt den gesamten Bildschirm aus.
                AppNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    // --- KORREKTUR: Das Padding wird hier ENTFERNT, damit der NavHost die ganze Fläche einnimmt. ---
                    // Jeder Screen kümmert sich dann selbst um das Padding, falls nötig.
                    modifier = Modifier.fillMaxSize()
                )
                // Die Bottom Navigation Bar wird nur auf bestimmten Screens angezeigt.
                if (currentRoute in listOf(
                        Screen.Home.route,
                        Screen.Rankings.route,
                        Screen.Profile.route
                    )
                ) {
                    // Die Surface sorgt für den schwebenden Look mit Schatten und abgerundeten Ecken.
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter) // Positioniert die Leiste unten in der Mitte.
                            .padding(bottom = 20.dp, start = 6.dp, end = 6.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars) // Sorgt für Abstand zur Systemleiste.
                            .clip(RoundedCornerShape(50)),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        BottomNavBar(
                            currentRoute = currentRoute ?: Screen.Home.route,
                            mainPlayer = navBarState.mainPlayer,
                            onItemClick = { screenRoute ->
                                // Navigationslogik für die Bottom Bar.
                                navController.navigate(screenRoute) {
                                    // Verhindert, dass der BackStack bei wiederholtem Klick wächst.
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
