package com.oudja.bebedex.data.biberon

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.oudja.bebedex.data.biberon.Biberon
import com.oudja.bebedex.data.biberon.BiberonDao

@Database(entities = [Biberon::class], version = 1, exportSchema = false)
abstract class BiberonDatabase : RoomDatabase() {
    abstract fun biberonDao(): BiberonDao

    companion object {
        @Volatile
        private var INSTANCE: BiberonDatabase? = null

        fun getDatabase(context: Context): BiberonDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    BiberonDatabase::class.java,
                    "biberon_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
