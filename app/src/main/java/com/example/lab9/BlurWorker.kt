package com.example.lab9_24it270

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val appContext = applicationContext
        val resourceUri = inputData.getString("IMAGE_URI")
        val blurLevel = inputData.getInt("BLUR_LEVEL", 1)

        try {
            if (resourceUri.isNullOrEmpty()) return@withContext Result.failure()

            val resolver = appContext.contentResolver
            val bitmap = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )

            val blurFactor = when (blurLevel) {
                1 -> 0.3f 
                2 -> 0.1f
                3 -> 0.03f
                else -> 0.1f
            }

            val output = blurBitmap(bitmap, blurFactor)

            // Lưu vào thư viện ảnh (Gallery)
            val outputUri = saveImageToGallery(appContext, output)

            Result.success(workDataOf("KEY_IMAGE_URI" to outputUri.toString()))
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }

    private fun blurBitmap(bitmap: Bitmap, factor: Float): Bitmap {
        val width = (bitmap.width * factor).toInt().coerceAtLeast(1)
        val height = (bitmap.height * factor).toInt().coerceAtLeast(1)
        val smallBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        return Bitmap.createScaledBitmap(smallBitmap, bitmap.width, bitmap.height, true)
    }

    private fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? {
        val filename = "Blur_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null
        val contentResolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/BlurImages")
            }
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { contentResolver.openOutputStream(it) }
        } else {
            val imagesDir = File(context.getExternalFilesDir(null)?.absolutePath + "/BlurImages")
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
            imageUri = Uri.fromFile(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return imageUri
    }
}