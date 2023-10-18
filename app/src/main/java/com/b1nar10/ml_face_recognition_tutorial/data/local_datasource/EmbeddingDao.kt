package com.b1nar10.ml_face_recognition_tutorial.data.local_datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EmbeddingDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertEmbedding(embeddingEntity: EmbeddingEntity): Long

    @Query("SELECT * FROM embedding")
    fun getEmbeddings(): List<EmbeddingEntity>
}