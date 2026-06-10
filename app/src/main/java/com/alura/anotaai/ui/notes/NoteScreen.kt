package com.alura.anotaai.ui.notes

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alura.anotaai.R
import com.alura.anotaai.extensions.audioDisplay
import com.alura.anotaai.ui.camera.CameraInitializer
import com.alura.anotaai.utils.PermissionUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    noteToEdit: String? = null,
    onBack: () -> Unit = {},
    onStartRecording: (String) -> Unit = {},
    onStopRecording: () -> Unit = {},
    onPlayAudio: (String) -> Unit = {},
    onStopAudio: () -> Unit = {},
) {
    val viewModel = hiltViewModel<NoteViewModel>()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val permissionUtils by remember { mutableStateOf(PermissionUtils(context)) }
    var permissionGranted by remember { mutableStateOf(permissionUtils.microphonePermissionsGranted()) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            permissionGranted = true
        }
    }

    LaunchedEffect(Unit) {
        noteToEdit?.let {
            viewModel.getNoteById(it)
        }
    }

    LaunchedEffect(state.isRecording) {
        if (state.addAudioNote) {
            viewModel.addNewItemAudio()
        }

        if (!state.isRecording) {
            viewModel.updateAudioDuration(0)
        } else {
            repeat(Int.MAX_VALUE) {
                viewModel.updateAudioDuration(state.audioDuration + 1)
                delay(1000)
            }
        }
    }

    LaunchedEffect(state.transcriptionError) {
        state.transcriptionError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    BasicTextField(
                        value = state.noteTextAppBar,
                        onValueChange = { viewModel.updateNoteTextAppBar(it) },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Apagar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = { viewModel.saveNote { onBack() } }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Salvar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp
            ) {
                Crossfade(targetState = state.isRecording, label = "recording_state") { isRecording ->
                    if (isRecording) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.Red, RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Gravando: ${state.audioDuration.audioDisplay()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            FilledIconButton(
                                onClick = {
                                    onStopRecording()
                                    viewModel.updateIsRecording(false)
                                    viewModel.updateAddAudioNote(true)
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(painterResource(R.drawable.ic_stop), contentDescription = "Parar")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = { viewModel.updateShowCameraState(true) }) {
                                Icon(painterResource(R.drawable.ic_camera), contentDescription = "Câmera")
                            }
                            
                            IconButton(onClick = { /* Galeria */ }) {
                                Icon(painterResource(R.drawable.ic_gallery), contentDescription = "Galeria")
                            }

                            Surface(
                                onClick = {
                                    if (permissionGranted) {
                                        val audioPath = "${context.externalCacheDir?.absolutePath}/audio${System.currentTimeMillis()}.m4a"
                                        viewModel.setAudioPath(audioPath)
                                        onStartRecording(audioPath)
                                        viewModel.updateIsRecording(true)
                                    } else {
                                        requestPermissionLauncher.launch(PermissionUtils.MICROPHONE_PERMISSIONS)
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(painterResource(R.drawable.ic_mic), contentDescription = "Gravar", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }

                            IconButton(
                                onClick = { viewModel.addNewItemText() },
                                enabled = state.noteText.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send, 
                                    contentDescription = "Enviar",
                                    tint = if (state.noteText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            ListNotes(
                modifier = Modifier.fillMaxWidth(),
                noteState = state.note,
                noteText = state.noteText,
                onNoteTextChanged = { viewModel.updateNoteText(it) },
                onPlayAudio = onPlayAudio,
                onStopAudio = onStopAudio,
                onTranscribeAudio = { audioPath -> viewModel.transcribeAudio(audioPath) },
                isTranscribing = state.isTranscribing,
                onUpdatedItem = { updateItem, id -> viewModel.updateItemText(updateItem, id) },
                onDeletedItem = { itemNote -> viewModel.deleteItemNote(itemNote) }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir nota?", fontWeight = FontWeight.Bold) },
            text = { Text("Você perderá todo o conteúdo desta nota permanentemente.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteNote { onBack() }
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (state.showCameraScreen) {
        CameraInitializer(
            onImageSaved = { filePath ->
                viewModel.addNewItemImage(filePath)
                viewModel.updateShowCameraState(false)
            },
            onError = {
                Toast.makeText(context, "Erro ao abrir câmera", Toast.LENGTH_SHORT).show()
                viewModel.updateShowCameraState(false)
            }
        )
    }
}
