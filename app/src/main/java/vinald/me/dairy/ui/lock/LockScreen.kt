package vinald.me.dairy.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import vinald.me.dairy.security.BiometricAuth

@Composable
fun LockScreen(
    viewModel: LockViewModel = viewModel(factory = LockViewModel.Factory),
) {
    val pin by viewModel.pin.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()

    val activity = LocalContext.current as? FragmentActivity
    val canUseBiometric = biometricEnabled && activity != null && BiometricAuth.isAvailable(activity)

    fun showBiometricPrompt() {
        if (activity == null) return
        BiometricAuth.prompt(
            activity = activity,
            onSuccess = viewModel::onBiometricSuccess,
            onFallback = {},
        )
    }

    LaunchedEffect(canUseBiometric) {
        if (canUseBiometric) showBiometricPrompt()
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.height(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (error) "Wrong PIN, try again" else "Enter your PIN",
                style = MaterialTheme.typography.titleMedium,
                color = if (error) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            PinDots(length = pin.length)
            Spacer(Modifier.height(48.dp))
            PinPad(
                onDigit = viewModel::onDigit,
                onBackspace = viewModel::onBackspace,
                onBiometric = if (canUseBiometric) ({ showBiometricPrompt() }) else null,
            )
        }
    }
}
