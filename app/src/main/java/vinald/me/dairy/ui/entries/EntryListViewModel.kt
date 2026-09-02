package vinald.me.dairy.ui.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import vinald.me.dairy.data.DiaryRepository
import vinald.me.dairy.ui.appContainer

data class EntryListUiState(
    val entries: List<EntryListItem> = emptyList(),
    val loading: Boolean = true,
)

class EntryListViewModel(private val repository: DiaryRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EntryListUiState> = _query
        .debounce { if (it.isEmpty()) 0L else 200L }
        .flatMapLatest { q -> repository.search(q) }
        .map { list ->
            EntryListUiState(
                entries = list.map { it.toListItem(repository::photoFile) },
                loading = false,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntryListUiState())

    fun onQueryChange(value: String) {
        _query.value = value
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { EntryListViewModel(appContainer.diaryRepository) }
        }
    }
}
