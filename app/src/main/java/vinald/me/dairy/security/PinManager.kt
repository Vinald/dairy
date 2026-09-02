package vinald.me.dairy.security

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.lockDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_lock")

/** Stores the app-lock PIN (as a salted PBKDF2 hash) and lock preferences. */
class PinManager(context: Context) {

    private val dataStore = context.applicationContext.lockDataStore

    val hasPin: Flow<Boolean> = dataStore.data.map { it[KEY_HASH] != null }

    val biometricEnabled: Flow<Boolean> =
        dataStore.data.map { it[KEY_BIOMETRIC] == true }

    suspend fun hasPinNow(): Boolean = hasPin.first()

    suspend fun setPin(pin: String) {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash(pin, salt)
        dataStore.edit {
            it[KEY_SALT] = salt.encode()
            it[KEY_HASH] = hash.encode()
        }
    }

    suspend fun verify(pin: String): Boolean {
        val prefs = dataStore.data.first()
        val salt = prefs[KEY_SALT]?.decode() ?: return false
        val hash = prefs[KEY_HASH]?.decode() ?: return false
        return PinHasher.matches(pin, salt, hash)
    }

    suspend fun clearPin() {
        dataStore.edit {
            it.remove(KEY_SALT)
            it.remove(KEY_HASH)
            it.remove(KEY_BIOMETRIC)
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_BIOMETRIC] = enabled }
    }

    private fun ByteArray.encode(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.decode(): ByteArray = Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        val KEY_SALT = stringPreferencesKey("pin_salt")
        val KEY_HASH = stringPreferencesKey("pin_hash")
        val KEY_BIOMETRIC = booleanPreferencesKey("biometric_enabled")
    }
}
