package com.oudja.bebedex.data.biberon

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BiberonDao {
    @Insert suspend fun insert(biberon: Biberon)
    @Update suspend fun update(biberon: Biberon)
    @Query("DELETE FROM biberons") suspend fun clearAll()
    @Query("SELECT * FROM biberons ORDER BY heure DESC") fun getAll(): Flow<List<Biberon>>
}
