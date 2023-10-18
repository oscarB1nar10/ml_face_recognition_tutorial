package com.b1nar10.ml_face_recognition_tutorial.data.local_datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PersonaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPersona(persona: PersonaEntity): Long

    @Query("SELECT * FROM persona WHERE persona_id = :personaId")
    fun getPersona(personaId: String): PersonaEntity

    @Query("SELECT persona_id FROM persona")
    fun getPersonaIds(): List<String>
}