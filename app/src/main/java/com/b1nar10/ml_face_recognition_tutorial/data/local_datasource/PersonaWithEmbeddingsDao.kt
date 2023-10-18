package com.b1nar10.ml_face_recognition_tutorial.data.local_datasource

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PersonaWithEmbeddingsDao {
    @Transaction
    @Query("SELECT * FROM persona WHERE persona_id = :personaId")
    fun getPersonaWithEmbeddings(personaId: String): PersonEmbeddingsEntity

    @Transaction
    @Query("SELECT * FROM persona")
    fun getPersonaWithEmbeddings(): PersonEmbeddingsEntity
}