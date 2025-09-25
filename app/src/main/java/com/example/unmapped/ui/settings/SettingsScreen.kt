package com.example.unmapped.ui.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

/**
 * Definiert eine versiegelte Klasse zur strukturierten Darstellung von formatierten Textinhalten.
 */
sealed class InfoContentElement {
    data class Heading(val text: String) : InfoContentElement()
    data class Paragraph(val text: String) : InfoContentElement()
    data class BulletPoint(val text: String) : InfoContentElement()
}

/**
 * Stellt den Bildschirm für die App-Einstellungen und Projektinformationen dar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
    val timerDuration by settingsViewModel.timerDuration.collectAsStateWithLifecycle()
    val shownTopic by settingsViewModel.shownInfoTopic.collectAsStateWithLifecycle()

    // Zeigt das Info-Overlay an, wenn ein Thema im ViewModel ausgewählt ist.
    shownTopic?.let { topic ->
        InfoOverlay(
            topic = topic,
            onDismissRequest = { settingsViewModel.hideInfoTopic() }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings & Info") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("Settings")
            SettingCard {
                DifficultySetting(
                    selectedMillis = timerDuration,
                    onOptionSelected = { settingsViewModel.setTimerDuration(it) }
                )
            }
            SettingCard {
                DarkModeSwitch(
                    checked = isDarkMode,
                    onCheckedChange = { settingsViewModel.setDarkMode(it) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("About the Project")
            InfoButton(
                text = "Projektidee & Funktionen",
                icon = Icons.Default.Lightbulb,
                onClick = { settingsViewModel.showInfoTopic(InfoTopic.PROJECT_IDEA) }
            )
            InfoButton(
                text = "Technische Umsetzung",
                icon = Icons.Default.Code,
                onClick = { settingsViewModel.showInfoTopic(InfoTopic.TECHNICAL) }
            )
            InfoButton(
                text = "How To Play",
                icon = Icons.Default.HelpOutline,
                onClick = { settingsViewModel.showInfoTopic(InfoTopic.HOW_TO_PLAY) }
            )
            InfoButton(
                text = "User Experience & Design",
                icon = Icons.Default.Palette,
                onClick = { settingsViewModel.showInfoTopic(InfoTopic.UX_UI) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Stellt eine formatierte Überschrift für eine Sektion dar.
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

/**
 * Stellt einen klickbaren Button dar, der ein Info-Overlay öffnet.
 */
@Composable
fun InfoButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Stellt einen Dialog dar, der detaillierte Informationen zu einem ausgewählten Thema anzeigt.
 */
@Composable
fun InfoOverlay(topic: InfoTopic, onDismissRequest: () -> Unit) {
    // Final überarbeitete Texte in der neuen Struktur
    val content: List<InfoContentElement> = when (topic) {
        InfoTopic.PROJECT_IDEA -> listOf(
            InfoContentElement.Heading("Projektidee & Inhalt"),
            InfoContentElement.Paragraph(
                "GEOPARDY – Because Ignorance Deserves a Stage ist eine satirische Neuinterpretation des GeoGuessr-Prinzips. " +
                        "Anstatt geografisches Wissen zu belohnen, stellt die App Fehler und Unwissenheit humorvoll in den Mittelpunkt. " +
                        "Das zentrale Designprinzip lautet ironische Gamification: Je schlechter die Tipps, desto unterhaltsamer wird das Spielerlebnis. " +
                        "Ein bewusst frecher Gen-Z-Ton durchzieht alle Textelemente, von den Roasts nach jeder Runde bis hin zu den Badge-Beschreibungen."
            ),
            InfoContentElement.Heading("Zentrale Funktionen"),
            InfoContentElement.BulletPoint("Spielmodi: Ein klassischer Einzelspielermodus, ein lokaler Multiplayer-Modus sowie thematisch kuratierte Karten und Challenges."),
            InfoContentElement.BulletPoint("Ranking-System (Loserboard): Eine globale Rangliste, die Spieler absteigend nach ihrem Gesamt-Strafpunktestand sortiert."),
            InfoContentElement.BulletPoint("Profil & Statistiken: Ein persönlicher Bereich mit anpassbarem Namen/Avatar, der „Hall of Shame“ (Badges) und detaillierte „Loser Metrics“ enthält."),
            InfoContentElement.BulletPoint("Badges & Progression: Über 20 Badges, die spezifische Arten des Scheiterns belohnen (z. B. „U.S. American“, „Flat Earther“).")
        )
        InfoTopic.TECHNICAL -> listOf(
            InfoContentElement.Paragraph(
                "Die App wurde nativ für Android mit Kotlin entwickelt und setzt vollständig auf das moderne, deklarative UI-Framework Jetpack Compose."
            ),
            InfoContentElement.Heading("Architektur & Navigation"),
            InfoContentElement.BulletPoint("Die App-Struktur basiert auf einer Single-Activity-Architektur unter Nutzung von Jetpack Navigation Compose."),
            InfoContentElement.BulletPoint("Die Architektur folgt dem MVVM-Muster, wobei ViewModels an Navigationsgraphen gekoppelt sind, um den Zustand über zusammengehörige Screens hinweg zu teilen."),
            InfoContentElement.Heading("Datenmanagement & Persistenz"),
            InfoContentElement.BulletPoint("Spielerdaten, Badges und Statistiken werden über SharedPreferences gespeichert, wobei Gson zur Serialisierung von Objekten eingesetzt wird."),
            InfoContentElement.BulletPoint("Ein zentraler DataResetManager kapselt die Logik zum Zurücksetzen aller Nutzerdaten."),
            InfoContentElement.Heading("Standortdaten & Logik"),
            InfoContentElement.BulletPoint(
                "Kuratiertes Standort-Set: Da Google keine vollständige Liste Street-View-fähiger Koordinaten bereitstellt, wurde ein Datensatz von ca. 50.000 Koordinaten von Kaggle heruntergeladen. " +
                        "Mithilfe eines eigenen Java-Skripts wurde dieser auf ca. 22.000 Street-View-fähige Locations vorgefiltert und in locations.txt gespeichert. " +
                        "Für den Challenge Mode wurde ein zweiter Datensatz mit ca. 10.000 Locations aus Kaggle-Daten erstellt (challenge-locations.txt)."
            ),
            InfoContentElement.BulletPoint(
                "API-Integration: Street View wird über das Google Maps SDK eingebunden. Die Nutzung des vorgefilterten Datensatzes reduziert unnötige API-Requests und optimiert die Performance."
            ),
            InfoContentElement.BulletPoint("Hilfsklassen kapseln die Logik zur Umwandlung von Koordinaten in Länder- und Kontinentinformationen mittels Android Geocoder."),
            InfoContentElement.Heading("Technisches Design & UI/UX"),
            InfoContentElement.BulletPoint("Die App basiert auf Material Design 3 mit vollständigem Light- und Dark-Mode-Support."),
            InfoContentElement.BulletPoint("Das Design ist stark Card-basiert und nutzt Schatten zur Schaffung einer klaren visuellen Hierarchie."),
            InfoContentElement.BulletPoint("Eine Edge-to-Edge-Implementierung sorgt für eine immersive Benutzeroberfläche."),
            InfoContentElement.BulletPoint("Der Charakter der App wird durch individuelle, KI-generierte Grafiken für die Badges unterstrichen."),
            InfoContentElement.BulletPoint("Eine schwebende Bottom Navigation Bar wird als modernes UI-Muster eingesetzt.")
        )
        InfoTopic.HOW_TO_PLAY -> listOf(
            InfoContentElement.Paragraph(
                "Die Regeln sind einfach, aber der Humor ist scharf – es geht weniger darum, zu beweisen, was du weißt, und mehr darum, darüber zu lachen, was du nicht weißt."
            ),
            InfoContentElement.BulletPoint("1. Eine zufällige Location auf der Erde wird per Google Street View enthüllt."),
            InfoContentElement.BulletPoint("2. Platziere deinen Marker auf der Weltkarte dort, wo du den Ort vermutest."),
            InfoContentElement.BulletPoint("3. Bestätige deinen Tipp. Die App berechnet, wie weit du daneben lagst."),
            InfoContentElement.BulletPoint("4. Verdiene ShamePoints: Je weiter weg, desto höher dein Score."),
            InfoContentElement.BulletPoint("5. Sammle Badges, lese sarkastische Kommentare und klettere das Loserboard empor.")
        )
        InfoTopic.UX_UI -> listOf(
            InfoContentElement.Heading("So funktioniert GEOPARDY"),
            InfoContentElement.Paragraph(
                "Der Einstieg in die App ist kontextsensitiv: Beim ersten Start wird ein Profil erstellt, danach landet der Nutzer immer direkt auf dem Home-Screen mit einer persönlichen Begrüßung. " +
                        "Die Navigation erfolgt über eine moderne, schwebende Bottom Navigation Bar."
            ),
            InfoContentElement.BulletPoint("Spielmodi starten: Über den Home-Screen können alle Spielmodi direkt ausgewählt werden."),
            InfoContentElement.BulletPoint("Ranking-Tab (Loserboard): Spieler können sich global vergleichen. Der Spieler an der Spitze ist der Champion of Failure."),
            InfoContentElement.BulletPoint("Profil-Tab: Dieser Bereich bündelt alle persönlichen Informationen, Badges und Statistiken."),
            InfoContentElement.Heading("User Experience (UX) Design"),
            InfoContentElement.Paragraph(
                "Das UX-Konzept kombiniert klassische Usability-Prinzipien mit einem satirischen Narrativ, das Fehler zelebriert."
            ),
            InfoContentElement.BulletPoint("Emotionale Bindung: Negatives Feedback wird durch humorvolle Roasts zu einem unterhaltsamen Kernelement."),
            InfoContentElement.BulletPoint("Umgekehrte Gamification: Das Loserboard und Badges für Fehler motivieren auf unkonventionelle Weise."),
            InfoContentElement.BulletPoint("Modernes und klares Design: Ein Edge-to-Edge-Layout, das Card-basierte Design und konsistente Farbgebung für eine intuitive Nutzerführung kombiniert."),
            InfoContentElement.BulletPoint("Hoher Wiederspielwert: Ein großer Pool an zufälligen Texten für Roasts und Kommentare sorgt dafür, dass sich die App bei jeder Nutzung frisch anfühlt.")
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(24.dp)
                ) {
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    FormattedInfoContent(content = content)
                }
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                        .height(50.dp)
                ) {
                    Text("Verstanden")
                }
            }
        }
    }
}

/**
 * Rendert eine Liste von `InfoContentElement`-Objekten mit passender Formatierung.
 */
@Composable
fun FormattedInfoContent(content: List<InfoContentElement>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        content.forEach { element ->
            when (element) {
                is InfoContentElement.Heading -> {
                    Text(
                        text = element.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, top = 8.dp)
                    )
                }
                is InfoContentElement.Paragraph -> {
                    Text(
                        text = element.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Justify,
                        lineHeight = 24.sp
                    )
                }
                is InfoContentElement.BulletPoint -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Top)
                        )
                        Text(
                            text = element.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Stellt eine Container-Karte für eine einzelne Einstellung dar.
 */
@Composable
fun SettingCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

/**
 * Stellt eine Gruppe von Radio-Buttons zur Auswahl der Timer-Dauer dar.
 */
@Composable
fun DifficultySetting(selectedMillis: Int, onOptionSelected: (Int) -> Unit) {
    val options = mapOf(
        "30 seconds (Hard)" to 30000,
        "1 minute (Normal)" to 60000,
        "2 minutes (Easy)" to 120000
    )
    Column(Modifier.selectableGroup()) {
        Text("Time Limit per Round", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        options.forEach { (text, millis) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (millis == selectedMillis),
                        onClick = { onOptionSelected(millis) },
                        role = Role.RadioButton
                    )
                    .clip(RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (millis == selectedMillis),
                    onClick = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Stellt den Schalter zum Aktivieren/Deaktivieren des Dark Modes dar.
 */
@Composable
fun DarkModeSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onCheckedChange(!checked) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Enable Dark Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Reduce eye strain in low light.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = { onCheckedChange(it) })
    }
}