package vinald.me.dairy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import vinald.me.dairy.data.entity.DiaryEntry
import vinald.me.dairy.data.entity.EntryPhoto

@Database(
    entities = [DiaryEntry::class, EntryPhoto::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DiaryDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao

    companion object {
        fun build(context: Context): DiaryDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                DiaryDatabase::class.java,
                "diary.db",
            ).build()
    }
}
