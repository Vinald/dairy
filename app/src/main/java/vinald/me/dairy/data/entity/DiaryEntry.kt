package vinald.me.dairy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    /** Mood.level, 1..5. */
    val moodLevel: Int,
    /** The day this entry is written for. */
    val entryDate: LocalDate,
    val createdAt: Instant,
    val updatedAt: Instant,
)
