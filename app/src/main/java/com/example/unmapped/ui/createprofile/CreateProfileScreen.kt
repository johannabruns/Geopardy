package com.example.unmapped.ui.createprofile

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.unmapped.R
import com.example.unmapped.data.Player
import com.example.unmapped.data.PlayerRepository
import com.example.unmapped.navigation.Screen
import com.example.unmapped.ui.theme.Yellow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * Der ViewModel, der die Zustands- und Geschäftslogik für den [CreateProfileScreen] verwaltet.
 */
class CreateProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val playerRepository = PlayerRepository(application)
    private val profilePrefs = application.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    var username by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null)

    private val _navigateToHome = MutableSharedFlow<Unit>()
    val navigateToHome = _navigateToHome.asSharedFlow()

    /**
     * Aktualisiert den Benutzernamen im Zustand des ViewModels.
     * @param newName Der neue Benutzername.
     */
    fun onUsernameChange(newName: String) {
        username = newName
    }

    /**
     * Aktualisiert die Bild-URI im Zustand des ViewModels.
     * @param newUri Die neue URI des ausgewählten Bildes.
     */
    fun onImageUriChange(newUri: Uri?) {
        imageUri = newUri
    }

    /**
     * Speichert das neue Profil. Erstellt einen neuen Spieler, speichert ihn im Repository,
     * legt ihn als Hauptspieler fest und löst die Navigation zum Home-Bildschirm aus.
     */
    fun saveProfile() {
        viewModelScope.launch {
            val newPlayer = Player(name = username.ifBlank { "Player" }, imageUri = imageUri?.toString())
            playerRepository.updatePlayer(newPlayer)
            profilePrefs.edit().putString("main_player_id", newPlayer.id).apply()
            // Sendet ein einmaliges Ereignis, um die Navigation auszulösen.
            _navigateToHome.emit(Unit)
        }
    }
}

/**
 * Eine Factory-Klasse zur Erstellung von [CreateProfileViewModel]-Instanzen.
 */
class CreateProfileViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    /**
     * Erstellt eine neue Instanz des ViewModels.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Stellt die Benutzeroberfläche zur Erstellung eines neuen Benutzerprofils dar.
 *
 * @param navController Der [NavController] zur Steuerung der Navigation.
 * @param modifier Ein [Modifier] zur Anpassung des Layouts.
 * @param viewModel Der [CreateProfileViewModel], der den Zustand für diesen Bildschirm hält.
 */
@Composable
fun CreateProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CreateProfileViewModel = viewModel(
        factory = CreateProfileViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isButtonEnabled = viewModel.username.isNotBlank() && viewModel.imageUri != null

    // Dieser Effekt lauscht auf das `MapsToHome`-Ereignis vom ViewModel, um zur Home-Ansicht zu navigieren.
    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collect {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.CreateProfile.route) { inclusive = true }
            }
        }
    }

    /**
     * Kopiert eine Datei von einer temporären Quell-URI in den permanenten Speicher der App.
     * @param sourceUri Die temporäre URI (z.B. vom Photo Picker).
     * @return Eine neue, permanente URI, die auf die kopierte Datei verweist, oder null bei einem Fehler.
     */
    fun copyUriToPermanentStorage(sourceUri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            // Erstellt ein Unterverzeichnis "profile_images" im privaten Speicher der App.
            val permanentDir = File(context.filesDir, "profile_images").apply { mkdirs() }
            val permanentFile = File(permanentDir, "${UUID.randomUUID()}.jpg")

            inputStream?.use { input -> permanentFile.outputStream().use { output -> input.copyTo(output) } }
            Uri.fromFile(permanentFile)
        } catch (e: Exception) {
            Log.e("CreateProfileScreen", "Error copying file", e)
            null
        }
    }

    // Initialisiert den ActivityResultLauncher für den Android Photo Picker.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { sourceUri: Uri? ->
        sourceUri?.let {
            val permanentUri = copyUriToPermanentStorage(it)
            viewModel.onImageUriChange(permanentUri)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            // Entfernt den Fokus vom Textfeld, wenn irgendwo anders auf dem Bildschirm geklickt wird.
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.onSecondary)
        ) {
            // Dekorative Hintergrundbox im oberen Bereich.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Die zentrale Eingabekarte.
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Construct your chaos.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Klickbarer Bereich für die Auswahl des Profilbilds.
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(color= MaterialTheme.colorScheme.tertiaryContainer)
                            .border(4.dp, Yellow, CircleShape)
                            .clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = viewModel.imageUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Zeigt ein Kamera-Icon an, solange kein Bild ausgewählt ist.
                        if (viewModel.imageUri == null) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = "Add Picture",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = viewModel.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = { Text("What should we call this disaster?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::saveProfile,
                        enabled = isButtonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Off you go.")
                    }
                }
            }
        }
    }
}