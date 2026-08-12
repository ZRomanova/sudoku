package com.zoya.sudoku.ui.navigation

object Routes {
    const val HOME = "home"
    const val CONSTRUCTOR = "constructor"
    const val CONSTRUCTOR_TIPS = "constructor_tips"
    const val LIBRARY = "library"
    const val GAME = "game"
    const val GAME_ARG_PUZZLE_ID = "puzzleId"
    const val GAME_PATTERN = "$GAME/{$GAME_ARG_PUZZLE_ID}"
    const val STATS = "stats"
    const val IN_PROGRESS = "in_progress"
    const val SETTINGS = "settings"

    fun game(puzzleId: Long) = "$GAME/$puzzleId"
}
