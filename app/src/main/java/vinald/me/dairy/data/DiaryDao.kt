package vinald.me.dairy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import vinald.me.dairy.data.entity.DiaryEntry
import vinald.me.dairy.data.entity.EntryPhoto
import vinald.me.dairy.data.entity.EntryWithPhotos
import java.time.LocalDate

@Dao
interface DiaryDao {

    @Transaction
    @Query("SELECT * FROM entries ORDER BY entryDate DESC, createdAt DESC")
    fun observeEntries(): Flow<List<EntryWithPhotos>>

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    fun observeEntry(id: Long): Flow<EntryWithPhotos?>

    @Transaction
    @Query(
        "SELECT * FROM entries WHERE entryDate BETWEEN :start AND :end " +
            "ORDER BY entryDate DESC, createdAt DESC",
    )
    fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<EntryWithPhotos>>

    @Transaction
    @Query(
        "SELECT * FROM entries " +
            "WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' " +
            "ORDER BY entryDate DESC, createdAt DESC",
    )
    fun search(query: String): Flow<List<EntryWithPhotos>>

    @Query("SELECT DISTINCT entryDate FROM entries WHERE entryDate BETWEEN :start AND :end")
    fun observeEntryDates(start: LocalDate, end: LocalDate): Flow<List<LocalDate>>

    @Upsert
    suspend fun upsert(entry: DiaryEntry): Long

    @Insert
    suspend fun insertPhotos(photos: List<EntryPhoto>)

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: Long)

    @Query("SELECT * FROM photos WHERE entryId = :entryId")
    suspend fun photosFor(entryId: Long): List<EntryPhoto>

    @Delete
    suspend fun delete(entry: DiaryEntry)
}
