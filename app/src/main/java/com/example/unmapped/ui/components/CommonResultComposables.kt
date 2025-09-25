package com.example.unmapped.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.unmapped.ui.theme.MediumPurple

/**
 * Stellt die obere Karte auf einem Ergebnisbildschirm dar.
 * Wird typischerweise für den "Roast"-Text oder eine andere prominente Nachricht verwendet.
 *
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 * @param roastText Der optional anzuzeigende "Roast"-Text.
 * @param content Ein optionaler Slot für benutzerdefinierten Inhalt anstelle des Roast-Textes.
 */
@Composable
fun ResultHeaderCard(
    modifier: Modifier = Modifier,
    roastText: String? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MediumPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (roastText != null) {
                Text(
                    text = roastText,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            if (content != null) {
                content()
            }
        }
    }
}

/**
 * Stellt die untere Karte auf einem Ergebnisbildschirm dar.
 * Sie dient als Container für Detailinformationen wie Distanz, Punkte und Aktionsbuttons.
 *
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 * @param content Der Inhalt, der innerhalb der Karte angezeigt werden soll.
 */
@Composable
fun ResultDetailsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}