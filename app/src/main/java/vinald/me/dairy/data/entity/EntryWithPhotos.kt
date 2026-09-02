package vinald.me.dairy.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class EntryWithPhotos(
    @Embedded val entry: DiaryEntry,
    @Relation(parentColumn = "id", entityColumn = "entryId")
    val photos: List<EntryPhoto>,
)
