package vinald.me.dairy.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    @Test
    fun correctPinMatches() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("1234", salt)
        assertTrue(PinHasher.matches("1234", salt, hash))
    }

    @Test
    fun wrongPinDoesNotMatch() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("1234", salt)
        assertFalse(PinHasher.matches("0000", salt, hash))
    }

    @Test
    fun differentSaltsProduceDifferentHashes() {
        val a = PinHasher.hash("1234", PinHasher.newSalt())
        val b = PinHasher.hash("1234", PinHasher.newSalt())
        assertNotEquals(a.toList(), b.toList())
    }
}
