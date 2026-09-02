package vinald.me.dairy.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Stores entry photos as files under filesDir/photos. */
class PhotoStorage(private val context: Context) {

    private val dir: File = File(context.filesDir, "photos").apply { mkdirs() }

    fun fileFor(fileName: String): File = File(dir, fileName)

    /** Copies [uri] into local storage and returns the generated file name. */
    suspend fun import(uri: Uri): String = withContext(Dispatchers.IO) {
        val fileName = "${UUID.randomUUID()}.jpg"
        val target = File(dir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not open $uri")
        fileName
    }

    fun delete(fileName: String) {
        File(dir, fileName).delete()
    }
}
