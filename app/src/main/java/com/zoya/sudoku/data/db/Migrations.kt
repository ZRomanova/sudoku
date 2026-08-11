package com.zoya.sudoku.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds game_results (Stats feature) - region_layouts and puzzle_state are untouched, so the
 *  player's saved layouts and any in-progress puzzle survive the upgrade. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `game_results` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`layoutId` INTEGER NOT NULL, " +
                "`difficulty` TEXT NOT NULL, " +
                "`correct` INTEGER NOT NULL, " +
                "`completedAt` INTEGER NOT NULL)"
        )
    }
}
