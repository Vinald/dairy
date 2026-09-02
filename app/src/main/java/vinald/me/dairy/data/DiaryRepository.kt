package vinald.me.dairy.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import vinald.me.dairy.data.entity.DiaryEntry
import vinald.me.dairy.data.entity.EntryPhoto
import vinald.me.dairy.data.entity.EntryWithPhotos
import java.io.File
import java.time.LocalDate

/** Single entry point to diary data for the UI layer. */
class DiaryRepository(
    private val dao: DiaryDao,
    private val photoStorage: PhotoStorage,
) {

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

    fun photoFile(fileName: String): File = photoStorage.fileFor(fileName)

    suspend fun save(entry: DiaryEntry): Long = dao.upsert(entry)

    suspend fun addPhotos(entryId: Long, uris: List<Uri>) {
        if (uris.isEmpty()) return
        val startPosition = dao.photosFor(entryId).size
        val rows = uris.mapIndexed { index, uri ->
            EntryPhoto(
                entryId = entryId,
                fileName = photoStorage.import(uri),
                position = startPosition + index,
            )
        }
        dao.insertPhotos(rows)
    }

    suspend fun removePhoto(photo: EntryPhoto) {
        dao.deletePhoto(photo.id)
        photoStorage.delete(photo.fileName)
    }

    suspend fun delete(entry: DiaryEntry) {
        dao.photosFor(entry.id).forEach { photoStorage.delete(it.fileName) }
        dao.delete(entry)
    }
}
