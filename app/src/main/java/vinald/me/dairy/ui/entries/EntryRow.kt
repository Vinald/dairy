package vinald.me.dairy.ui.entries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import vinald.me.dairy.data.Mood
import vinald.me.dairy.data.entity.EntryWithPhotos
import vinald.me.dairy.ui.formatMedium
import java.io.File
import java.time.LocalDate

data class EntryListItem(
    val id: Long,
    val title: String,
    val snippet: String,
    val date: LocalDate,
    val mood: Mood,
    val photoCount: Int,
    val firstPhoto: File?,
)

fun EntryWithPhotos.toListItem(resolvePhoto: (String) -> File) = EntryListItem(
    id = entry.id,
    title = entry.title,
    snippet = entry.body.replace('\n', ' ').trim().take(140),
    date = entry.entryDate,
    mood = Mood.fromLevel(entry.moodLevel),
    photoCount = photos.size,
    firstPhoto = photos.minByOrNull { it.position }?.let { resolvePhoto(it.fileName) },
)

@Composable
fun EntryRow(
    item: EntryListItem,
    onClick: () -> Unit,
    showDate: Boolean = true,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(item.title.ifBlank { "(untitled)" }, maxLines = 1) },
        overlineContent = if (showDate) {
            { Text(item.date.formatMedium()) }
        } else {
            null
        },
        supportingContent = {
            if (item.snippet.isNotBlank()) Text(item.snippet, maxLines = 2)
        },
        leadingContent = {
            Text(item.mood.emoji, style = MaterialTheme.typography.headlineSmall)
        },
        trailingContent = {
            item.firstPhoto?.let { file ->
                AsyncImage(
                    model = file,
                    contentDescription = if (item.photoCount > 1) {
                        "${item.photoCount} photos"
                    } else {
                        "1 photo"
                    },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            }
        },
    )
}
