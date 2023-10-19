package com.b1nar10.ml_face_recognition_tutorial

import android.graphics.Bitmap
import com.b1nar10.ml_face_recognition_tutorial.data.FaceAnalyzerRepository
import com.b1nar10.ml_face_recognition_tutorial.data.model.PersonModel

class FaceAnalyzerImpl : FaceAnalyzerRepository {
    override suspend fun analyzeFaceImage(bitmapImage: Bitmap): PersonModel {
        TODO("Not yet implemented")
    }

    override suspend fun saveNewFace(personModel: PersonModel): Boolean {
        TODO("Not yet implemented")
    }
}