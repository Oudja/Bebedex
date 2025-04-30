package com.oudja.bebedex

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BebeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val gender: String,
    val level: Int = 1,
    val hp: Int = 100,
    val xp: Int = 0
)
