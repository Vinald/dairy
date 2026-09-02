package vinald.me.dairy.ui.entries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vinald.me.dairy.data.Mood
import vinald.me.dairy.data.entity.EntryWithPhotos
import vinald.me.dairy.ui.formatMedium
import java.time.LocalDate

data class EntryListItem(
    val id: Long,
    val title: String,
    val snippet: String,
    val date: LocalDate,
    val mood: Mood,
    val photoCount: Int,
)

fun EntryWithPhotos.toListItem() = EntryListItem(
    id = entry.id,
    title = entry.title,
    snippet = entry.body.replace('\n', ' ').trim().take(140),
    date = entry.entryDate,
    mood = Mood.fromLevel(entry.moodLevel),
    photoCount = photos.size,
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
            if (item.photoCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(item.photoCount.toString())
                }
            }
        },
    )
}
