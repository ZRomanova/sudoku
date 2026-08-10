package com.zoya.sudoku.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RegionLayoutEntity::class, PuzzleStateEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun regionLayoutDao(): RegionLayoutDao
    abstract fun puzzleStateDao(): PuzzleStateDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sudoku.db"
                )
                    // Personal, pre-release app with no real user data to preserve across schema
                    // changes - simplest to reset rather than hand-write migrations.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
