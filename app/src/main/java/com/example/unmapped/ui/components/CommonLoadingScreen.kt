package com.example.unmapped.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Stellt ein wiederverwendbares Layout für Ladebildschirme zur Verfügung.
 * Besteht aus einem großen Bild im oberen Bereich und einer zentrierten Karte für den Inhalt (Text, Ladeanzeige).
 *
 * @param backgroundImageRes Die Ressourcen-ID des anzuzeigenden Hintergrundbildes.
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 * @param content Der spezifische Inhalt (z.B. Text und Ladeanzeige), der in der Karte angezeigt werden soll.
 */
@Composable
fun GameLoadingScreen(
    @DrawableRes backgroundImageRes: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Der Bild-Header im oberen Bereich des Bildschirms.
        Image(
            painter = painterResource(id = backgroundImageRes),
            contentDescription = "Loading background",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
            contentScale = ContentScale.Crop
        )

        // Der untere Bereich, der den Inhalt in einer zentrierten Karte darstellt.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    content()
                }
            }
        }
    }
}