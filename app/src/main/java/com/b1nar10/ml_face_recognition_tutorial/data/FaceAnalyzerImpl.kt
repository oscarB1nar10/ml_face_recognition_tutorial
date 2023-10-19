package com.b1nar10.ml_face_recognition_tutorial.data

import android.graphics.Bitmap
import android.util.Log
import com.b1nar10.ml_face_recognition_tutorial.data.local_datasource.EmbeddingDao
import com.b1nar10.ml_face_recognition_tutorial.data.local_datasource.EmbeddingEntity
import com.b1nar10.ml_face_recognition_tutorial.data.local_datasource.PersonaDao
import com.b1nar10.ml_face_recognition_tutorial.data.local_datasource.PersonaEntity
import com.b1nar10.ml_face_recognition_tutorial.data.model.PersonModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import javax.inject.Inject
import kotlin.math.pow

class FaceAnalyzerImpl @Inject constructor(
    private val interpreter: Interpreter,
    private val personaDao: PersonaDao,
    private val embeddingDao: EmbeddingDao
) : FaceAnalyzerRepository {
    private val TAG = "FaceAnalyzerImpl"
    private val tensorImage: TensorImage = TensorImage(DataType.FLOAT32)


    // Analyzes a face image to recognize the person
    override suspend fun analyzeFaceImage(bitmapImage: Bitmap): PersonModel {
        // Normalize the image
        val tensorImage = normalizeImage(bitmapImage)
        val outputEmbeddingSize = Array(1) { FloatArray(FEATURE_VECTOR_SIZE) }
        interpreter.run(tensorImage.buffer, outputEmbeddingSize)

        // Match the embedding against the database
        val recognizedName = findClosestMatch(outputEmbeddingSize[0])

        return PersonModel(personName = recognizedName, bitmapImage = bitmapImage)
    }


    override suspend fun saveNewFace(personModel: PersonModel): Boolean {
        return try {
            val tensorImage = normalizeImage(personModel.bitmapImage)
            val outputEmbeddingSize = Array(1) { FloatArray(FEATURE_VECTOR_SIZE) }
            interpreter.run(tensorImage.buffer, outputEmbeddingSize)

            // Serialize the embedding and save to the databse
            val serializedEncoding = EmbeddingConverter.fromFloatArrayToJson(outputEmbeddingSize[0])
            val persona = PersonaEntity(id = personModel.id, name = personModel.personName)
            val personInserted = personaDao.insertPersona(persona)
            println("PersonaInserted: $personInserted")

            val embeddingSaved = embeddingDao.insertEmbedding(
                EmbeddingEntity(
                    encoding = serializedEncoding,
                    personaId = personModel.id
                )
            )

            println("embeddingSaved: $embeddingSaved")

            // Return true to indicate success
            true
        } catch (e: Exception) {
            // Log the error and return false to indicate failure
            Log.e(TAG, " Error saving new face: ", e)
            false
        }
    }

    // Find the closest embedding for the given embedding
    private fun findClosestMatch(embedding: FloatArray): String {
        try {
            val knownEmbeddings = embeddingDao.getEmbeddings()

            var minDistance = Float.MAX_VALUE
            var userId = ""

            // Calculate the distance between the input embedding and each saved embedding
            for (knownEmbedding in knownEmbeddings) {
                val distance =
                    calculateDistance(
                        embedding,
                        EmbeddingConverter.fromJsonToFloatArray(knownEmbedding.encoding)
                    )
                if (distance < minDistance) {
                    minDistance = distance
                    userId = knownEmbedding.personaId
                }
            }

            // Use a threshold to decide if the dace is recognized or unknown
            val recognizedThreshold = RECOGNITION_THRESHOLD
            return if (minDistance < recognizedThreshold) {
                personaDao.getPersona(userId).name
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            return ""
        }
    }

    // Calculate the euclidean distance between two embeddings
    private fun calculateDistance(embedding1: FloatArray, embedding2: FloatArray): Float {
        return kotlin.math.sqrt(
            embedding1.zip(embedding2).sumOf { (a, b) -> (a - b).toDouble().pow(2.0) }.toFloat()
        )
    }

    // Normalizes and preprocess the input image to make it suitable for the model
    private fun normalizeImage(bitmapImage: Bitmap): TensorImage {
        // Define the preprocessing operations
        val imageProcessor = ImageProcessor.Builder()
            .add(
                ResizeWithCropOrPadOp(
                    IMAGE_TARGET_SIZE,
                    IMAGE_TARGET_SIZE
                )
            )  // Crop/pad the image to the target size
            .add(
                ResizeOp(
                    IMAGE_TARGET_SIZE,
                    IMAGE_TARGET_SIZE,
                    ResizeOp.ResizeMethod.NEAREST_NEIGHBOR
                )
            )  // Resize to the target size
            .add(NormalizeOp(0f, 255f))  // Normalize pixel values
            .build()

        // Apply the operations
        tensorImage.load(bitmapImage)
        return imageProcessor.process(tensorImage)
    }

    companion object {
        const val IMAGE_TARGET_SIZE = 224
        const val FEATURE_VECTOR_SIZE = 1280
        const val RECOGNITION_THRESHOLD = 10.0f
    }
}