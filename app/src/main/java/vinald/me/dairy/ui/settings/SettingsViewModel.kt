package vinald.me.dairy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vinald.me.dairy.data.AppPreferences
import vinald.me.dairy.security.PinManager
import vinald.me.dairy.ui.appContainer

class SettingsViewModel(
    private val pinManager: PinManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    val hasPin: StateFlow<Boolean> = pinManager.hasPin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val biometricEnabled: StateFlow<Boolean> = pinManager.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val dynamicColor: StateFlow<Boolean> = appPreferences.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { pinManager.setBiometricEnabled(enabled) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setDynamicColor(enabled) }
    }

    fun removeLock() {
        viewModelScope.launch { pinManager.clearPin() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SettingsViewModel(appContainer.pinManager, appContainer.appPreferences)
            }
        }
    }
}
