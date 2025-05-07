package dev.aurakai.auraframefx.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

object ImageUtils {

    /**
     * Saves a bitmap to the app's cache directory and returns the file path
     */
    fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        fileName: String = "decal_${UUID.randomUUID()}.png",
    ): String {
        return File(context.cacheDir, fileName).apply {
            outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }
        }.absolutePath
    }

    /**
     * Loads a bitmap from a file path
     */
    fun loadBitmapFromPath(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Loads a bitmap from a content URI
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts a bitmap to a byte array
     */
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * Gets a bitmap from a byte array
     */
    fun getBitmapFromByteArray(byteArray: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    /**
     * Gets the MIME type of an image URI
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    /**
     * Creates a temporary file in the cache directory
     */
    fun createTempImageFile(
        context: Context,
        prefix: String = "img_",
        suffix: String = ".png",
    ): File {
        val storageDir = context.cacheDir
        return File.createTempFile(prefix, suffix, storageDir)
    }
}
