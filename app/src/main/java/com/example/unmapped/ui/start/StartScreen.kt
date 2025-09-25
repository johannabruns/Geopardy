package com.example.unmapped.ui.start

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.unmapped.data.DataResetManager
import com.example.unmapped.navigation.Screen

/**
 * Stellt den Startbildschirm der App dar (auch als Splash-Screen bezeichnet).
 * Von hier aus wird entschieden, ob der Benutzer zur Profilerstellung oder zum Hauptmenü geleitet wird.
 *
 * @param navController Der NavController zur Steuerung der Navigation.
 * @param modifier Ein Modifier zur Anpassung des Layouts.
 */
@Composable
fun StartScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Leere Box, um den Titel und den Button korrekt zu positionieren.
        Box(modifier = Modifier.weight(1f))

        // Zentraler Bereich mit App-Titel und Slogan.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(2f)
        ) {
            Text(
                text = "Geopardy",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 70.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Because ignorance deserves a stage.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // Button am unteren Bildschirmrand.
        Button(
            onClick = {
                // Prüft, ob bereits ein Hauptspieler-Profil existiert.
                val profilePrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                val mainPlayerId = profilePrefs.getString("main_player_id", null)

                val destination = if (mainPlayerId == null) {
                    // Wenn kein Profil existiert, werden alle alten Daten gelöscht und zur Profilerstellung navigiert.
                    DataResetManager.resetAllData(context)
                    Screen.CreateProfile.route
                } else {
                    // Andernfalls wird direkt zum Home-Bildschirm navigiert.
                    Screen.Home.route
                }

                navController.navigate(destination) {
                    // Entfernt den StartScreen aus dem Backstack, sodass man nicht dorthin zurückkehren kann.
                    popUpTo(Screen.Start.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .weight(1f, fill = false),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Get roasted.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}