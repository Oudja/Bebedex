package com.oudja.bebedex.data.biberon

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "biberons")
data class Biberon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quantiteMl: Int,
    val heure: Long
)
