package vinald.me.dairy.ui.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vinald.me.dairy.data.DiaryRepository
import vinald.me.dairy.data.entity.EntryWithPhotos
import vinald.me.dairy.ui.appContainer

class EntryDetailViewModel(
    private val repository: DiaryRepository,
    entryId: Long,
) : ViewModel() {

    val entry: StateFlow<EntryWithPhotos?> = repository.entry(entryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            entry.value?.let { repository.delete(it.entry) }
            onDeleted()
        }
    }

    companion object {
        fun factory(entryId: Long) = viewModelFactory {
            initializer { EntryDetailViewModel(appContainer.diaryRepository, entryId) }
        }
    }
}
