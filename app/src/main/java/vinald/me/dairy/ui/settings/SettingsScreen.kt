package vinald.me.dairy.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenPinSetup: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val hasPin by viewModel.hasPin.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ListItem(
                headlineContent = { Text("Dynamic colours") },
                supportingContent = { Text("Match the app palette to your wallpaper") },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = viewModel::setDynamicColor,
                    )
                },
            )

            if (!hasPin) {
                ListItem(
                    modifier = Modifier.clickable(onClick = onOpenPinSetup),
                    headlineContent = { Text("Set up app lock") },
                    supportingContent = { Text("Require a PIN to open the diary") },
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                )
            } else {
                ListItem(
                    modifier = Modifier.clickable(onClick = onOpenPinSetup),
                    headlineContent = { Text("Change PIN") },
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                )
                ListItem(
                    headlineContent = { Text("Unlock with biometrics") },
                    supportingContent = { Text("Use fingerprint or face to unlock") },
                    leadingContent = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = viewModel::setBiometricEnabled,
                        )
                    },
                )
                ListItem(
                    modifier = Modifier.clickable(onClick = viewModel::removeLock),
                    headlineContent = { Text("Remove app lock") },
                    leadingContent = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                )
            }
        }
    }
}
