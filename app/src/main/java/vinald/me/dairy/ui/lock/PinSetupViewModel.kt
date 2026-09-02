package vinald.me.dairy.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vinald.me.dairy.security.LockManager
import vinald.me.dairy.security.PinManager
import vinald.me.dairy.ui.appContainer

enum class PinSetupStage { ENTER, CONFIRM }

data class PinSetupState(
    val stage: PinSetupStage = PinSetupStage.ENTER,
    val pin: String = "",
    val mismatch: Boolean = false,
)

class PinSetupViewModel(
    private val pinManager: PinManager,
    private val lockManager: LockManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PinSetupState())
    val state: StateFlow<PinSetupState> = _state.asStateFlow()

    private var firstEntry: String = ""

    fun onDigit(digit: Char, onDone: () -> Unit) {
        val current = _state.value
        if (current.pin.length >= PIN_LENGTH) return
        val updated = current.copy(pin = current.pin + digit, mismatch = false)
        _state.value = updated
        if (updated.pin.length == PIN_LENGTH) advance(updated, onDone)
    }

    fun onBackspace() {
        _state.value = _state.value.copy(
            pin = _state.value.pin.dropLast(1),
            mismatch = false,
        )
    }

    private fun advance(state: PinSetupState, onDone: () -> Unit) {
        when (state.stage) {
            PinSetupStage.ENTER -> {
                firstEntry = state.pin
                _state.value = PinSetupState(stage = PinSetupStage.CONFIRM)
            }

            PinSetupStage.CONFIRM -> {
                if (state.pin == firstEntry) {
                    viewModelScope.launch {
                        pinManager.setPin(firstEntry)
                        lockManager.onPinJustSet()
                        onDone()
                    }
                } else {
                    _state.value = PinSetupState(stage = PinSetupStage.ENTER, mismatch = true)
                    firstEntry = ""
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { PinSetupViewModel(appContainer.pinManager, appContainer.lockManager) }
        }
    }
}
