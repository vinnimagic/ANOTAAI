package com.alura.anotaai.repository

import androidx.room.withTransaction
import com.alura.anotaai.database.AppDatabase
import com.alura.anotaai.database.entities.toNote
import com.alura.anotaai.database.entities.toNoteItemAudio
import com.alura.anotaai.database.entities.toNoteItemImage
import com.alura.anotaai.database.entities.toNoteItemText
import com.alura.anotaai.model.BaseNote
import com.alura.anotaai.model.Note
import com.alura.anotaai.model.NoteItemAudio
import com.alura.anotaai.model.NoteItemImage
import com.alura.anotaai.model.NoteItemText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class NoteRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val noteDao = database.noteDao()
    private val textNoteDao = database.textNoteDao()
    private val imageNoteDao = database.imageNoteDao()
    private val audioNoteDao = database.audioNoteDao()

    suspend fun addNote(note: Note) {
        database.withTransaction {
            // Salva ou atualiza a nota principal
            noteDao.insert(note.toNoteEntity())
            
            // Sincroniza os itens removendo os antigos e inserindo a lista atualizada
            textNoteDao.deleteByIdMainNote(note.id)
            imageNoteDao.deleteByIdMainNote(note.id)
            audioNoteDao.deleteByIdMainNote(note.id)
            
            note.listItems.forEach { noteItem ->
                when (noteItem) {
                    is NoteItemText -> textNoteDao.insert(
                        noteItem.toNoteTextEntity().copy(idMainNote = note.id)
                    )

                    is NoteItemImage -> imageNoteDao.insert(
                        noteItem.toNoteImageEntity().copy(idMainNote = note.id)
                    )

                    is NoteItemAudio -> audioNoteDao.insert(
                        noteItem.toAudioNoteEntity().copy(idMainNote = note.id)
                    )
                }
            }
        }
    }

    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
            .map { noteList -> 
                noteList.map { it.toNote() }.sortedByDescending { it.date } 
            }
    }

    suspend fun getNoteById(noteId: String): Note? {
        val noteEntity = noteDao.getNoteById(noteId) ?: return null
        val textNotes = textNoteDao.getByIdMainNote(noteEntity.id).map { it.toNoteItemText() }
        val imageNotes = imageNoteDao.getByIdMainNote(noteEntity.id).map { it.toNoteItemImage() }
        val audioNotes = audioNoteDao.getByIdMainNote(noteEntity.id).map { it.toNoteItemAudio() }
        
        return Note(
            id = noteEntity.id,
            title = noteEntity.title,
            date = noteEntity.date,
            listItems = (textNotes + imageNotes + audioNotes).sortedByDescending { it.date }
        )
    }

    suspend fun removeNote(note: Note) {
        database.withTransaction {
            noteDao.delete(note.toNoteEntity())
            textNoteDao.deleteByIdMainNote(note.id)
            imageNoteDao.deleteByIdMainNote(note.id)
            audioNoteDao.deleteByIdMainNote(note.id)
        }
    }

    suspend fun removeItemNote(noteItem: BaseNote) {
        when (noteItem) {
            is NoteItemText -> textNoteDao.delete(noteItem.id)
            is NoteItemImage -> imageNoteDao.delete(noteItem.id)
            is NoteItemAudio -> audioNoteDao.delete(noteItem.id)
        }
    }
}
