package com.b1nar10.ml_face_recognition_tutorial.data

import android.content.res.AssetManager
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

fun AssetManager.loadFileModel(modelPath: String): MappedByteBuffer {
    // Open the model file
    val fileDescriptor = this.openFd(modelPath)
    // Create an input stream from the file descriptor
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    // Create a file channel from the input stream
    val fileChannel = inputStream.channel
    // Get the start offset and declared length of the model file
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength

    // Returns a MappedByteBuffer that represents the model file
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}