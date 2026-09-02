package vinald.me.dairy.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    onDone: () -> Unit,
    viewModel: PinSetupViewModel = viewModel(factory = PinSetupViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App lock") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val message = when {
                state.mismatch -> "PINs didn't match. Start again."
                state.stage == PinSetupStage.ENTER -> "Choose a 4-digit PIN"
                else -> "Confirm your PIN"
            }
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = if (state.mismatch) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            PinDots(length = state.pin.length)
            Spacer(Modifier.height(48.dp))
            PinPad(
                onDigit = { digit -> viewModel.onDigit(digit, onDone) },
                onBackspace = viewModel::onBackspace,
            )
        }
    }
}
