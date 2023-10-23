package com.b1nar10.ml_face_recognition_tutorial.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

fun saveBitmapToTempFile(bitmap: Bitmap, context: Context): String {
    val tempFile = File(context.cacheDir, "tempFaceBitmap.png")
    val outStream = FileOutputStream(tempFile)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
    outStream.close()
    return tempFile.absolutePath
}