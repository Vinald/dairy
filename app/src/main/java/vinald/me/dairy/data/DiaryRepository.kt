package vinald.me.dairy.data

import kotlinx.coroutines.flow.Flow
import vinald.me.dairy.data.entity.DiaryEntry
import vinald.me.dairy.data.entity.EntryWithPhotos
import java.time.LocalDate

/** Single entry point to diary data for the UI layer. */
class DiaryRepository(private val dao: DiaryDao) {

    fun entries(): Flow<List<EntryWithPhotos>> = dao.observeEntries()

    fun entry(id: Long): Flow<EntryWithPhotos?> = dao.observeEntry(id)

    fun entriesBetween(start: LocalDate, end: LocalDate): Flow<List<EntryWithPhotos>> =
        dao.observeEntriesBetween(start, end)

    fun entryDates(start: LocalDate, end: LocalDate): Flow<List<LocalDate>> =
        dao.observeEntryDates(start, end)

    fun search(query: String): Flow<List<EntryWithPhotos>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) dao.observeEntries() else dao.search(trimmed)
    }

    suspend fun save(entry: DiaryEntry): Long = dao.upsert(entry)

    suspend fun delete(entry: DiaryEntry) = dao.delete(entry)
}
