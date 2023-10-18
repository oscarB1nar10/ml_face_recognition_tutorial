package com.b1nar10.ml_face_recognition_tutorial.data.local_datasource

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "embedding",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["persona_id"],
            childColumns = ["personOwnerId"],
        )
    ],
    indices = [Index(value = ["personOwnerId"])]
)
data class EmbeddingEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "embedding_id") val id: Int? = null,
    @ColumnInfo(name = "encoding") val encoding: String,
    @ColumnInfo(name = "personOwnerId") val personaId: String
)
