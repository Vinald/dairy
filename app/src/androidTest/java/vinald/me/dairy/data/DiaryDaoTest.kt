package vinald.me.dairy.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import vinald.me.dairy.data.entity.DiaryEntry
import vinald.me.dairy.data.entity.EntryPhoto
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DiaryDaoTest {

    private lateinit var db: DiaryDatabase
    private lateinit var dao: DiaryDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DiaryDatabase::class.java,
        ).build()
        dao = db.diaryDao()
    }

    @After
    fun tearDown() = db.close()

    private fun entry(title: String, body: String, date: LocalDate) = DiaryEntry(
        title = title,
        body = body,
        moodLevel = 3,
        entryDate = date,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )

    @Test
    fun upsertAndObserve() = runBlocking {
        dao.upsert(entry("First", "hello world", LocalDate.of(2026, 1, 5)))
        val all = dao.observeEntries().first()
        assertEquals(1, all.size)
        assertEquals("First", all[0].entry.title)
    }

    @Test
    fun searchMatchesTitleOrBody() = runBlocking {
        dao.upsert(entry("Groceries", "bought milk", LocalDate.of(2026, 1, 1)))
        dao.upsert(entry("Walk", "sunny day", LocalDate.of(2026, 1, 2)))
        assertEquals(1, dao.search("milk").first().size)
        assertEquals(1, dao.search("Walk").first().size)
        assertEquals(0, dao.search("nothing").first().size)
    }

    @Test
    fun entriesBetweenFiltersByDate() = runBlocking {
        dao.upsert(entry("Jan", "a", LocalDate.of(2026, 1, 15)))
        dao.upsert(entry("Feb", "b", LocalDate.of(2026, 2, 15)))
        val jan = dao.observeEntriesBetween(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
        ).first()
        assertEquals(1, jan.size)
        assertEquals("Jan", jan[0].entry.title)
    }

    @Test
    fun deletingEntryCascadesToPhotos() = runBlocking {
        val id = dao.upsert(entry("Trip", "photos", LocalDate.of(2026, 3, 1)))
        dao.insertPhotos(listOf(EntryPhoto(entryId = id, fileName = "a.jpg", position = 0)))
        assertEquals(1, dao.photosFor(id).size)
        dao.delete(dao.observeEntry(id).first()!!.entry)
        assertTrue(dao.photosFor(id).isEmpty())
    }
}
