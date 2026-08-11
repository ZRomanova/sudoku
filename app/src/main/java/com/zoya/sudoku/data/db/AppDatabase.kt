package com.zoya.sudoku.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RegionLayoutEntity::class, PuzzleStateEntity::class, GameResultEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun regionLayoutDao(): RegionLayoutDao
    abstract fun puzzleStateDao(): PuzzleStateDao
    abstract fun gameResultDao(): GameResultDao

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
                    // The player's saved layouts are real data worth keeping, so version bumps
                    // that matter get a hand-written migration; anything else still falls back to
                    // a destructive reset rather than blocking on a migration nobody wrote.
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
