package com.b1nar10.ml_face_recognition_tutorial.di

import android.app.Application
import android.speech.tts.TextToSpeech
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {

    @Provides
    fun provideTextToSpeech(context: Application): TextToSpeech {
        lateinit var textToSpeech: TextToSpeech
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.US
            }
        }
        return textToSpeech
    }

    @Provides
    fun provideExecutorService(): ExecutorService {
        return  Executors.newSingleThreadExecutor()
    }

    @Provides
    fun provideFaceDetector(): FaceDetector {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()

        return FaceDetection.getClient(options)
    }
}