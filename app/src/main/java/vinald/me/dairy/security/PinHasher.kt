package vinald.me.dairy.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Pure PBKDF2 helpers for the app-lock PIN. No Android dependencies. */
object PinHasher {

    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun newSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun hash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    /** Constant-time comparison. */
    fun matches(pin: String, salt: ByteArray, expectedHash: ByteArray): Boolean {
        val actual = hash(pin, salt)
        if (actual.size != expectedHash.size) return false
        var diff = 0
        for (i in actual.indices) diff = diff or (actual[i].toInt() xor expectedHash[i].toInt())
        return diff == 0
    }
}
