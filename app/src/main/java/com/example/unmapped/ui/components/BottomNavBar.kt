package com.example.unmapped.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.unmapped.R
import com.example.unmapped.data.Player
import com.example.unmapped.navigation.Screen

/**
 * Stellt die untere Navigationsleiste der App dar.
 * Sie enthält Links zu den Hauptbereichen (Home, Rankings, Profil) und hebt den aktiven Tab hervor.
 *
 * @param currentRoute Die Route des aktuell angezeigten Bildschirms.
 * @param mainPlayer Das Objekt des Hauptspielers, um ggf. das Profilbild anzuzeigen.
 * @param onItemClick Eine Callback-Funktion, die beim Klick auf ein Navigationselement aufgerufen wird.
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    mainPlayer: Player?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(68.dp),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Liste der Navigationselemente für eine einfache Iteration.
            val navItems = listOf(
                Screen.Home to "home",
                Screen.Rankings to "rankings",
                Screen.Profile to "profile"
            )

            navItems.forEach { (screen, label) ->
                val selected = currentRoute == screen.route

                // Jedes Element nimmt ein Drittel der verfügbaren Breite ein.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Deaktiviert den visuellen Klick-Effekt (Ripple).
                            onClick = { onItemClick(screen.route) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Zeigt entweder das Profilbild oder ein Standard-Icon an.
                    if (screen == Screen.Profile && mainPlayer?.imageUri != null) {
                        AsyncImage(
                            model = mainPlayer.imageUri.toUri(),
                            contentDescription = label,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(
                                id = when (screen) {
                                    Screen.Home -> R.drawable.icon_house_chimney_black
                                    Screen.Rankings -> R.drawable.icon_podium_black
                                    else -> R.drawable.icon_gamer_black
                                }
                            ),
                            contentDescription = label,
                            modifier = Modifier.size(26.dp),
                            tint = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = label,
                        color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}