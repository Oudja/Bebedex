package com.oudja.bebedex.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo


@Entity
data class BebeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val gender: String,
    val level: Int = 1,
    val hp: Int = 100,
    val xp: Int = 0,
    val dateNaissance: String = "",
    val heureNaissance: String = "",
    val taille: Float = 50f,
    val poids: Float = 3.5f
)