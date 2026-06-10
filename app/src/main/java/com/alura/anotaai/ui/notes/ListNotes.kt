package com.alura.anotaai.ui.notes

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alura.anotaai.R
import com.alura.anotaai.extensions.audioDisplay
import com.alura.anotaai.model.BaseNote
import com.alura.anotaai.model.Note
import com.alura.anotaai.model.NoteItemAudio
import com.alura.anotaai.model.NoteItemImage
import com.alura.anotaai.model.NoteItemText
import com.alura.anotaai.model.NoteType
import kotlinx.coroutines.delay

@Composable
fun ListNotes(
    modifier: Modifier = Modifier,
    noteText: String = "",
    onNoteTextChanged: (String) -> Unit = {},
    noteState: Note = Note(),
    onPlayAudio: (String) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onTranscribeAudio: (String) -> Unit = {},
    isTranscribing: Boolean = false,
    onUpdatedItem: (String, String) -> Unit = { _, _ -> },
    onDeletedItem: (BaseNote) -> Unit = {}
) {
    val stateList = rememberLazyListState()
    var itemToDelete by remember { mutableStateOf<BaseNote?>(null) }

    LazyColumn(
        modifier = modifier,
        state = stateList,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                BasicTextField(
                    value = noteText,
                    onValueChange = { onNoteTextChanged(it) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (noteText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.write_your_note),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        innerTextField()
                    }
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        items(noteState.listItems.sortedByDescending { it.date }, key = { it.id }) { item ->
            when (item.type) {
                NoteType.TEXT -> {
                    ItemNoteText(
                        item = item as NoteItemText,
                        onUpdated = { updatedItemText ->
                            onUpdatedItem(updatedItemText, item.id)
                        },
                        onDeleted = { itemToDelete = item }
                    )
                }
                NoteType.IMAGE -> {
                    ItemNoteImage(
                        item = item as NoteItemImage,
                        onDeleted = { itemToDelete = item }
                    )
                }
                NoteType.AUDIO -> {
                    ItemNoteAudio(
                        item = item as NoteItemAudio,
                        onPlayAudio = onPlayAudio,
                        onStopAudio = onStopAudio,
                        onTranscribeAudio = onTranscribeAudio,
                        isTranscribing = isTranscribing,
                        onDeleted = { itemToDelete = item }
                    )
                }
            }
        }
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(text = "Excluir item?") },
            text = { Text("Deseja remover este conteúdo da nota?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeletedItem(itemToDelete!!)
                    itemToDelete = null
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ItemNoteText(
    modifier: Modifier = Modifier,
    item: NoteItemText,
    onUpdated: (String) -> Unit,
    onDeleted: () -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var stateText by remember { mutableStateOf(item.content) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isEditing = true },
                    onLongPress = { onDeleted() }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        if (isEditing) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    modifier = Modifier.weight(1f),
                    value = stateText,
                    onValueChange = { stateText = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                )
                IconButton(onClick = {
                    isEditing = false
                    onUpdated(stateText)
                }) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Confirmar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Text(
                text = item.content,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ItemNoteImage(
    modifier: Modifier = Modifier,
    item: NoteItemImage,
    onDeleted: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { expanded = !expanded },
                    onLongPress = { onDeleted() }
                )
            },
        shape = RoundedCornerShape(24.dp)
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxWidth().aspectRatio(if (expanded) 0.75f else 1.5f),
            model = item.link,
            contentScale = ContentScale.Crop,
            contentDescription = "Imagem"
        )
    }
}

@Composable
private fun ItemNoteAudio(
    modifier: Modifier = Modifier,
    item: NoteItemAudio,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    onTranscribeAudio: (String) -> Unit = {},
    isTranscribing: Boolean = false,
    onDeleted: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(false) }
    val icon = if (isPlaying) Icons.Filled.Close else Icons.Filled.PlayArrow

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            delay(item.duration * 1000L)
            isPlaying = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onDeleted() })
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Áudio • ${item.duration.audioDisplay()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            
            Row {
                if (isTranscribing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(4.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { onTranscribeAudio(item.link) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_title),
                            contentDescription = "Transcrever",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            onStopAudio()
                            isPlaying = false
                        } else {
                            onPlayAudio(item.link)
                            isPlaying = true
                        }
                    }
                ) {
                    Icon(imageVector = icon, contentDescription = "Play/Stop", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
