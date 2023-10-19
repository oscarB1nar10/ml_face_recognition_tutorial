package com.b1nar10.ml_face_recognition_tutorial.di

import android.content.Context
import com.b1nar10.ml_face_recognition_tutorial.data.FaceAnalyzerImpl
import com.b1nar10.ml_face_recognition_tutorial.data.FaceAnalyzerRepository
import com.b1nar10.ml_face_recognition_tutorial.data.loadFileModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideTfModel(@ApplicationContext context: Context): MappedByteBuffer {
        return context.assets.loadFileModel("mobilenet_v2.tflite")
    }

    @Provides
    @Singleton
    fun provideInterpreter(mappedByteBuffer: MappedByteBuffer): Interpreter {
        return Interpreter(mappedByteBuffer)
    }

    @Provides
    @Singleton
    fun provideFaceAnalyzerRepository(
        faceAnalyzerImpl: FaceAnalyzerImpl
    ): FaceAnalyzerRepository = faceAnalyzerImpl
}








