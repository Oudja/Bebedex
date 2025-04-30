package com.oudja.bebedex

import androidx.room.*

@Dao
interface BebeDao {
    @Query("SELECT * FROM BebeEntity")
    suspend fun getAll(): List<BebeEntity>

    @Query("SELECT * FROM BebeEntity WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): BebeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bebe: BebeEntity)

    @Update
    suspend fun update(bebe: BebeEntity)

    @Query("DELETE FROM BebeEntity")
    suspend fun deleteAll()
}
