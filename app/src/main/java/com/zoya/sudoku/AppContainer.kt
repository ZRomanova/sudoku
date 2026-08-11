package com.zoya.sudoku

import android.content.Context
import com.zoya.sudoku.data.db.AppDatabase
import com.zoya.sudoku.data.repository.PuzzleRepository
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import com.zoya.sudoku.data.repository.StatsRepository

/** Lightweight manual DI container - no Hilt/Dagger needed for a project this size. */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val regionLayoutRepository = RegionLayoutRepository(database.regionLayoutDao())
    val puzzleRepository = PuzzleRepository(database.puzzleStateDao(), database.regionLayoutDao())
    val statsRepository = StatsRepository(database.gameResultDao(), regionLayoutRepository)
}
