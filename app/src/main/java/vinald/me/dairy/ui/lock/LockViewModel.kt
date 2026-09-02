package vinald.me.dairy.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vinald.me.dairy.security.LockManager
import vinald.me.dairy.security.PinManager
import vinald.me.dairy.ui.appContainer

class LockViewModel(
    private val pinManager: PinManager,
    private val lockManager: LockManager,
) : ViewModel() {

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()

    val biometricEnabled: StateFlow<Boolean> = pinManager.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun onDigit(digit: Char) {
        if (_pin.value.length >= PIN_LENGTH) return
        _error.value = false
        _pin.value += digit
        if (_pin.value.length == PIN_LENGTH) verify()
    }

    fun onBackspace() {
        _error.value = false
        _pin.value = _pin.value.dropLast(1)
    }

    private fun verify() {
        val entered = _pin.value
        viewModelScope.launch {
            if (pinManager.verify(entered)) {
                lockManager.unlock()
            } else {
                _error.value = true
                _pin.value = ""
            }
        }
    }

    fun onBiometricSuccess() = lockManager.unlock()

    companion object {
        val Factory = viewModelFactory {
            initializer { LockViewModel(appContainer.pinManager, appContainer.lockManager) }
        }
    }
}
