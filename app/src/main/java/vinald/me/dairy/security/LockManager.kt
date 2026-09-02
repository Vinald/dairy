package vinald.me.dairy.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tracks whether the app is currently locked. The app locks whenever it goes to
 * the background (if a PIN is set) and unlocks only after successful auth.
 */
class LockManager(private val pinManager: PinManager) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun start() {
        scope.launch { _isLocked.value = pinManager.hasPinNow() }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun unlock() {
        _isLocked.value = false
    }

    /** Called after a PIN is created so the user is not immediately locked out. */
    fun onPinJustSet() {
        _isLocked.value = false
    }

    override fun onStop(owner: LifecycleOwner) {
        scope.launch {
            if (pinManager.hasPinNow()) _isLocked.value = true
        }
    }
}
