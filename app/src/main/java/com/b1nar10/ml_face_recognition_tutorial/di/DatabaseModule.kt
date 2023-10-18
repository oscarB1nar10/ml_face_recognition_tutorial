package com.b1nar10.ml_face_recognition_tutorial.di

import android.content.Context
import androidx.room.Room
import com.b1nar10.ml_face_recognition_tutorial.data.local_datasource.AppDatabase
import com.b1nar10.ml_face_recognition_tutorial.data.local_datasource.EmbeddingDao
import com.b1nar10.ml_face_recognition_tutorial.data.local_datasource.PersonaDao
import com.b1nar10.ml_face_recognition_tutorial.data.local_datasource.PersonaWithEmbeddingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "face_recognition_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providesPersonaDao(database: AppDatabase): PersonaDao {
        return database.personDao()
    }

    @Provides
    fun providesEmbeddingsDao(database: AppDatabase): EmbeddingDao {
        return database.embeddingDao()
    }

    @Provides
    fun providesPersonaWithEmbeddingsDao(database: AppDatabase): PersonaWithEmbeddingsDao {
        return database.personaWithEmbeddingDao()
    }
}