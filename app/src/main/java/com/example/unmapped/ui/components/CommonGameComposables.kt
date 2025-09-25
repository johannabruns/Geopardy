package com.example.unmapped.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Stellt eine kleine, runde Karte dar, die zur Anzeige von Informationen während des Spiels
 * (z.B. Timer, Rundenanzahl) dient.
 *
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 * @param content Der Inhalt, der innerhalb der Karte angezeigt werden soll.
 */
@Composable
fun GameInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * Stellt einen runden Button mit einem "Schließen"-Icon dar.
 * Wird typischerweise verwendet, um ein Spiel zu verlassen.
 *
 * @param onClick Die Aktion, die beim Klick auf den Button ausgeführt wird.
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 */
@Composable
fun GameExitButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "Leave Game",
            modifier = Modifier
                .size(44.dp)
                .padding(10.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Stellt den primären Aktionsbutton am unteren Bildschirmrand während des Spiels dar
 * (z.B. zum Öffnen der Karte oder Bestätigen einer Auswahl).
 *
 * @param onClick Die Aktion, die beim Klick auf den Button ausgeführt wird.
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 * @param text Der Text, der auf dem Button angezeigt wird.
 */
@Composable
fun GameBottomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Plant your poor judgment."
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}