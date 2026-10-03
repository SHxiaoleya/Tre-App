package com.trea.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trea.app.data.notes.NoteItem
import com.trea.app.data.notes.NoteRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    val notes: StateFlow<List<NoteItem>> = repository.notesFlow

    init {
        viewModelScope.launch {
            repository.refreshNotes()
        }
    }

    fun saveNote(id: String?, title: String, content: String) {
        viewModelScope.launch {
            repository.saveNote(id, title, content)
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    companion object {
        fun provideFactory(
            repository: NoteRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotesViewModel(repository) as T
            }
        }
    }
}
