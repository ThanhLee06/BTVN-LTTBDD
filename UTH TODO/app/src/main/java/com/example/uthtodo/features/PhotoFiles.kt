package com.example.uthtodo.features

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object PhotoFiles {
    suspend fun import(context: Context, uri: Uri): TaskPhoto = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "File đã chọn không phải ảnh hợp lệ." }
        val options = BitmapFactory.Options().apply {
            inSampleSize = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 3200) inSampleSize *= 2
        }
        var bitmap = resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Không đọc được ảnh.")
        try {
            val orientation = resolver.openInputStream(uri).use { input ->
                input?.let { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
            } ?: ExifInterface.ORIENTATION_NORMAL
            val matrix = Matrix().apply { when (orientation) {
                2 -> setScale(-1f, 1f); 3 -> setRotate(180f); 4 -> setScale(1f, -1f)
                5 -> { setRotate(90f); postScale(-1f, 1f) }; 6 -> setRotate(90f)
                7 -> { setRotate(270f); postScale(-1f, 1f) }; 8 -> setRotate(270f)
            } }
            if (!matrix.isIdentity) {
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated !== bitmap) { bitmap.recycle(); bitmap = rotated }
            }
            val ratio = 1600f / maxOf(bitmap.width, bitmap.height)
            if (ratio < 1) {
                val scaled = Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt().coerceAtLeast(1),
                    (bitmap.height * ratio).toInt().coerceAtLeast(1), true)
                if (scaled !== bitmap) { bitmap.recycle(); bitmap = scaled }
            }
            val id = UUID.randomUUID().toString()
            val file = File(FeatureStore.photoDirectory(context), "$id.jpg")
            try { file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)) { "Không ghi được ảnh." } } }
            catch (e: Exception) { file.delete(); throw e }
            TaskPhoto(id, file.absolutePath, System.currentTimeMillis())
        } finally { bitmap.recycle() }
    }
}
