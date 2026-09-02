package vinald.me.dairy.ui.entries

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vinald.me.dairy.data.DiaryRepository
import vinald.me.dairy.data.Mood
import vinald.me.dairy.data.entity.DiaryEntry
import vinald.me.dairy.data.entity.EntryPhoto
import vinald.me.dairy.ui.appContainer
import java.io.File
import java.time.Instant
import java.time.LocalDate

data class EditorState(
    val loaded: Boolean = false,
    val title: String = "",
    val body: String = "",
    val mood: Mood = Mood.OKAY,
    val date: LocalDate = LocalDate.now(),
    val existingPhotos: List<EntryPhoto> = emptyList(),
    val newPhotos: List<Uri> = emptyList(),
    val saving: Boolean = false,
) {
    val canSave: Boolean get() = !saving && (title.isNotBlank() || body.isNotBlank())
}

class EntryEditorViewModel(
    private val repository: DiaryRepository,
    private val entryId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private var createdAt: Instant = Instant.now()
    private val removedPhotos = mutableListOf<EntryPhoto>()

    init {
        if (entryId == null) {
            _state.value = EditorState(loaded = true)
        } else {
            viewModelScope.launch {
                val existing = repository.entry(entryId).first()
                _state.value = if (existing == null) {
                    EditorState(loaded = true)
                } else {
                    createdAt = existing.entry.createdAt
                    EditorState(
                        loaded = true,
                        title = existing.entry.title,
                        body = existing.entry.body,
                        mood = Mood.fromLevel(existing.entry.moodLevel),
                        date = existing.entry.entryDate,
                        existingPhotos = existing.photos.sortedBy { it.position },
                    )
                }
            }
        }
    }

    fun photoFile(fileName: String): File = repository.photoFile(fileName)

    fun onTitleChange(value: String) = _state.update { it.copy(title = value) }

    fun onBodyChange(value: String) = _state.update { it.copy(body = value) }

    fun onMoodChange(mood: Mood) = _state.update { it.copy(mood = mood) }

    fun onDateChange(date: LocalDate) = _state.update { it.copy(date = date) }

    fun onPhotosPicked(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _state.update { it.copy(newPhotos = it.newPhotos + uris) }
    }

    fun onRemoveNewPhoto(uri: Uri) =
        _state.update { it.copy(newPhotos = it.newPhotos - uri) }

    fun onRemoveExistingPhoto(photo: EntryPhoto) {
        removedPhotos += photo
        _state.update { it.copy(existingPhotos = it.existingPhotos - photo) }
    }

    fun save(onSaved: (Long) -> Unit) {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val now = Instant.now()
            val id = repository.save(
                DiaryEntry(
                    id = entryId ?: 0L,
                    title = current.title.trim(),
                    body = current.body.trim(),
                    moodLevel = current.mood.level,
                    entryDate = current.date,
                    createdAt = if (entryId == null) now else createdAt,
                    updatedAt = now,
                ),
            )
            removedPhotos.forEach { repository.removePhoto(it) }
            repository.addPhotos(id, current.newPhotos)
            onSaved(id)
        }
    }

    companion object {
        fun factory(entryId: Long?) = viewModelFactory {
            initializer { EntryEditorViewModel(appContainer.diaryRepository, entryId) }
        }
    }
}
