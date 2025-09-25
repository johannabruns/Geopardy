package com.example.unmapped.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Definiert das Farbschema für den hellen Modus (Light Mode).
 */
private val LightAppColorScheme = lightColorScheme(
    primary = MediumPurple,
    secondary = Yellow,
    tertiary = Pink,
    background = OffWhite,
    surface = White,
    onPrimary = Color.White,
    onSecondary = DarkPurple,
    onTertiary = DarkPurple,
    onBackground = DarkPurple,
    onSurface = DarkPurple
)

/**
 * Definiert das Farbschema für den dunklen Modus (Dark Mode).
 */
private val DarkAppColorScheme = darkColorScheme(
    primary = MediumPurple,
    secondary = Yellow,
    tertiary = Pink,
    background = DarkGray,
    surface = Color(0xFF42424E), // Ein etwas helleres Grau für Oberflächen im Dark Mode
    onPrimary = Color.White,
    onSecondary = DarkGray,
    onTertiary = DarkGray,
    onBackground = OffWhite,
    onSurface = OffWhite
)

/**
 * Die zentrale Theme-Composable der App. Sie sollte die gesamte UI umschließen.
 * Sie wendet das passende Farbschema (hell oder dunkel) und die Typografie an.
 *
 * @param darkTheme True, wenn das dunkle Theme verwendet werden soll, andernfalls false.
 * @param content Der UI-Inhalt, auf den das Theme angewendet wird.
 */
@Composable
fun UnmappedTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkAppColorScheme
    } else {
        LightAppColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}