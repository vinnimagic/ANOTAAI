package com.alura.anotaai.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alura.anotaai.model.BaseNote
import com.alura.anotaai.model.NoteItemAudio
import com.alura.anotaai.model.NoteItemImage
import com.alura.anotaai.model.NoteItemText
import com.alura.anotaai.repository.NoteRepository
import com.alura.anotaai.service.TranscriptionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteUiState())
    var uiState = _uiState.asStateFlow()

    private val transcriptionService = TranscriptionService()

    companion object {
        private const val GROQ_API_KEY = "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"
    }

    fun getNoteById(noteId: String) {
        viewModelScope.launch {
            noteRepository.getNoteById(noteId)?.let {
                _uiState.value = NoteUiState(note = it, noteTextAppBar = it.title)
            }
        }
    }

    fun saveNote(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val currentState = _uiState.value
            // Atualiza a data para a nota subir na lista e o título atual do TopBar
            var noteToSave = currentState.note.copy(
                title = currentState.noteTextAppBar,
                date = System.currentTimeMillis()
            )

            // Se houver texto no campo de entrada mas não foi enviado à lista, captura agora
            if (currentState.noteText.isNotBlank()) {
                val listItems = noteToSave.listItems.toMutableList()
                listItems.add(
                    NoteItemText(
                        content = currentState.noteText,
                        date = System.currentTimeMillis()
                    )
                )
                noteToSave = noteToSave.copy(listItems = listItems)
                _uiState.value = currentState.copy(note = noteToSave, noteText = "")
            }

            noteRepository.addNote(noteToSave)
            onSuccess() // Só executa a navegação após o banco confirmar o salvamento
        }
    }

    fun deleteNote(onSuccess: () -> Unit) {
        viewModelScope.launch {
            noteRepository.removeNote(_uiState.value.note)
            onSuccess()
        }
    }

    fun deleteItemNote(noteItem: BaseNote) {
        viewModelScope.launch {
            noteRepository.removeItemNote(noteItem)
            updateCurrentNote()
        }
    }

    private fun updateCurrentNote() {
        viewModelScope.launch {
            _uiState.value.note.id.let {
                getNoteById(it)
            }
        }
    }

    fun updateNoteTextAppBar(text: String) {
        _uiState.value = _uiState.value.copy(noteTextAppBar = text)
    }

    fun addNewItemImage(imageLink: String) {
        val listItems = _uiState.value.note.listItems.toMutableList()
        listItems.add(NoteItemImage(link = imageLink, date = System.currentTimeMillis()))
        _uiState.value = _uiState.value.copy(note = _uiState.value.note.copy(listItems = listItems))
        saveNote()
    }

    fun addNewItemAudio() {
        val listItems = _uiState.value.note.listItems.toMutableList()
        listItems.add(
            NoteItemAudio(
                link = _uiState.value.audioPath,
                duration = _uiState.value.audioDuration,
                date = System.currentTimeMillis()
            )
        )
        _uiState.value = _uiState.value.copy(
            note = _uiState.value.note.copy(listItems = listItems),
            addAudioNote = false
        )
        saveNote()
    }

    fun addNewItemText() {
        if (_uiState.value.noteText.isBlank()) return
        val listItems = _uiState.value.note.listItems.toMutableList()
        listItems.add(
            NoteItemText(
                content = _uiState.value.noteText,
                date = System.currentTimeMillis()
            )
        )
        _uiState.value = _uiState.value.copy(
            note = _uiState.value.note.copy(listItems = listItems),
            noteText = ""
        )
        saveNote()
    }

    fun updateItemText(newText: String, id: String) {
        val updatedList = _uiState.value.note.listItems.map { item ->
            if (item.id == id && item is NoteItemText) item.copy(content = newText) else item
        }
        _uiState.value =
            _uiState.value.copy(note = _uiState.value.note.copy(listItems = updatedList))
        saveNote()
    }

    fun updateNoteText(text: String) {
        _uiState.value = _uiState.value.copy(noteText = text)
    }

    fun updateShowCameraState(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCameraScreen = show)
    }

    fun updateIsRecording(recording: Boolean) {
        _uiState.value = _uiState.value.copy(isRecording = recording)
    }

    fun updateAddAudioNote(add: Boolean) {
        _uiState.value = _uiState.value.copy(addAudioNote = add)
    }

    fun updateAudioDuration(newDuration: Int) {
        _uiState.value = _uiState.value.copy(audioDuration = newDuration)
    }

    fun setAudioPath(audioPath: String) {
        _uiState.value = _uiState.value.copy(audioPath = audioPath)
    }

    fun resetNote() {
        _uiState.value = NoteUiState()
    }

    fun transcribeAudio(audioFilePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTranscribing = true, transcriptionError = null)
            val result = transcriptionService.transcribeAudio(audioFilePath, GROQ_API_KEY)
            result.onSuccess { transcribedText ->
                val listItems = _uiState.value.note.listItems.toMutableList()
                listItems.add(
                    NoteItemText(
                        content = transcribedText,
                        date = System.currentTimeMillis()
                    )
                )
                _uiState.value = _uiState.value.copy(
                    note = _uiState.value.note.copy(listItems = listItems),
                    isTranscribing = false
                )
                saveNote()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isTranscribing = false,
                    transcriptionError = error.message
                )
            }
        }
    }
}
