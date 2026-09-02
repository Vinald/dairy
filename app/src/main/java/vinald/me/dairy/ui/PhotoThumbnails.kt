package vinald.me.dairy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Horizontal strip of photo thumbnails. Each entry of [photos] may be any
 * Coil-supported model: a [java.io.File] for a stored photo, a [android.net.Uri]
 * for a pending pick. When [onRemove] is supplied a delete badge is shown.
 */
@Composable
fun PhotoStrip(
    photos: List<Any>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onRemove: ((Any) -> Unit)? = null,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        items(photos) { model ->
            Box(Modifier.padding(end = 8.dp)) {
                AsyncImage(
                    model = model,
                    contentDescription = "Entry photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                if (onRemove != null) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                            .clickable { onRemove(model) }
                            .padding(4.dp),
                    )
                }
            }
        }
    }
}
