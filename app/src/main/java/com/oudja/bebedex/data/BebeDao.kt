package com.oudja.bebedex.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oudja.bebedex.data.BebeEntity

@Dao
interface BebeDao {
    @Query("SELECT * FROM BebeEntity")
    suspend fun getAll(): List<BebeEntity>

    @Query("SELECT * FROM BebeEntity WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): BebeEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(bebe: BebeEntity)

    @Update
    suspend fun update(bebe: BebeEntity)

    @Query("DELETE FROM BebeEntity")
    suspend fun deleteAll()
}