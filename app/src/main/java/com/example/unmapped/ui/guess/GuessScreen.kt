package com.example.unmapped.ui.guess

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unmapped.ui.theme.DarkPurple
import com.example.unmapped.ui.theme.MediumPurple
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Stellt ein bildschirmfüllendes Overlay mit einer Google Map dar, auf der der Spieler
 * seine Schätzung durch Platzieren eines Markers abgeben kann.
 *
 * @param onDismissRequest Callback, der aufgerufen wird, wenn das Overlay geschlossen werden soll.
 * @param onGuessConfirmed Callback, der die finale Position der Schätzung übergibt.
 * @param markerPosition Die aktuelle Position des Markers, oder null, wenn keiner gesetzt ist.
 * @param onMapClick Callback, der die Koordinaten eines Klicks auf der Karte übergibt.
 * @param isDismissible Legt fest, ob das Overlay durch Klick auf den Hintergrund geschlossen werden kann.
 */
@Composable
fun GuessOverlay(
    onDismissRequest: () -> Unit,
    onGuessConfirmed: (LatLng) -> Unit,
    markerPosition: LatLng?,
    onMapClick: (LatLng) -> Unit,
    isDismissible: Boolean = true
) {
    val isGuessMade = markerPosition != null
    val cameraPositionState = rememberCameraPositionState {
        // Initiale Kameraposition, die die Weltkarte zentriert anzeigt.
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 1.5f)
    }

    // Semi-transparenter Hintergrund, der das Schließen des Dialogs ermöglicht.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPurple.copy(alpha = 0.9f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (isDismissible) onDismissRequest() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Die Hauptkarte, die den Inhalt des Overlays enthält.
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f)
                .clickable(enabled = false, onClick = {}), // Verhindert, dass Klicks durch die Karte zum Hintergrund durchgehen.
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header-Bereich mit Titel und Schließen-Button.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MediumPurple, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Place your Guess",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Let’s see how wrong you can be.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    if (isDismissible) {
                        IconButton(onClick = onDismissRequest, modifier = Modifier.align(Alignment.CenterEnd)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Hilfetext, der mit einer Animation verschwindet, sobald ein Pin gesetzt wurde.
                AnimatedVisibility(
                    visible = !isGuessMade,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    Text(
                        text = "Tap on the map to place your pin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                // Ein leerer Platzhalter, der die Höhe des Hilfetextes einnimmt,
                // um Layout-Sprünge zu vermeiden, wenn der Text verschwindet.
                if(isGuessMade){
                    Spacer(modifier = Modifier.height(35.dp))
                }

                // Die Google Map Komponente.
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    cameraPositionState = cameraPositionState,
                    onMapClick = onMapClick
                ) {
                    // Der Marker wird nur angezeigt, wenn eine Position ausgewählt wurde.
                    if (isGuessMade) {
                        Marker(state = MarkerState(position = markerPosition!!), title = "Your Guess")
                    }
                }

                // Der Bestätigungsbutton.
                Button(
                    onClick = { markerPosition?.let { onGuessConfirmed(it) } },
                    // Der Button ist nur aktivierbar, wenn ein Marker gesetzt wurde.
                    enabled = isGuessMade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 16.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Seal your shame.", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}