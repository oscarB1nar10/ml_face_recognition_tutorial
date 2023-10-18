package com.b1nar10.ml_face_recognition_tutorial.data.local_datasource

import androidx.room.Embedded
import androidx.room.Relation

data class PersonEmbeddingsEntity(
    @Embedded
    val persona: PersonaEntity,

    @Relation(parentColumn = "persona_id", entityColumn = "personOwnerId")
    val embeddings: List<EmbeddingEntity>
)
