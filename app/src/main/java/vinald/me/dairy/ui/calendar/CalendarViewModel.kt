package vinald.me.dairy.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import vinald.me.dairy.data.DiaryRepository
import vinald.me.dairy.ui.appContainer
import vinald.me.dairy.ui.entries.EntryListItem
import vinald.me.dairy.ui.entries.toListItem
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val daysWithEntries: Set<LocalDate> = emptySet(),
    val selectedEntries: List<EntryListItem> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(private val repository: DiaryRepository) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val markedDays = month.flatMapLatest { ym ->
        repository.entryDates(ym.atDay(1), ym.atEndOfMonth())
    }

    private val entriesForSelectedDay = selectedDate.flatMapLatest { date ->
        repository.entriesBetween(date, date)
    }

    val uiState: StateFlow<CalendarUiState> = combine(
        month,
        selectedDate,
        markedDays,
        entriesForSelectedDay,
    ) { ym, date, days, entries ->
        CalendarUiState(
            month = ym,
            selectedDate = date,
            daysWithEntries = days.toSet(),
            selectedEntries = entries.map { it.toListItem(repository::photoFile) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun showPreviousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun showNextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        month.value = YearMonth.from(date)
        selectedDate.value = date
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { CalendarViewModel(appContainer.diaryRepository) }
        }
    }
}
