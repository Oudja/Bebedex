package com.oudja.bebedex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oudja.bebedex.data.BebeEntity

@Database(entities = [BebeEntity::class], version = 3, exportSchema = false)
abstract class BebeDatabase : RoomDatabase() {
    abstract fun bebeDao(): BebeDao

    companion object {
        @Volatile
        private var INSTANCE: BebeDatabase? = null

        // ✅ Migration de version 1 à 2
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE BebeEntity ADD COLUMN xp INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): BebeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BebeDatabase::class.java,
                    "bebe_database"
                )
                    .fallbackToDestructiveMigration(true) // ← conserve bien cette ligne
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}