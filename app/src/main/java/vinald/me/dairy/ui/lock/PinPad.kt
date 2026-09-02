package vinald.me.dairy.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

const val PIN_LENGTH = 4

@Composable
fun PinDots(length: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(PIN_LENGTH) { index ->
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .then(
                        if (index < length) {
                            Modifier.background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        },
                    ),
            )
        }
    }
}

@Composable
fun PinPad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    onBiometric: (() -> Unit)? = null,
) {
    val rows = listOf(('1'..'3'), ('4'..'6'), ('7'..'9'))
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { digit -> PadKey(digit.toString()) { onDigit(digit) } }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(72.dp), Alignment.Center) {
                if (onBiometric != null) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "Use biometric",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onBiometric),
                    )
                }
            }
            PadKey("0") { onDigit('0') }
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackspace),
                Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun PadKey(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.headlineSmall)
    }
}
