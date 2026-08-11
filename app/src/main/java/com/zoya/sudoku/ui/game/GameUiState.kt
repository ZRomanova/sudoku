package com.zoya.sudoku.ui.game

import com.zoya.sudoku.engine.Difficulty

sealed interface GameUiState {
    data object Loading : GameUiState

    data class Loaded(
        val layoutId: Long,
        val difficulty: Difficulty,
        val cellRegion: IntArray,
        val board: IntArray,
        val solution: IntArray,
        val givens: IntArray,
        val selectedCell: Int?,
        val showErrors: Boolean
    ) : GameUiState {
        val isFull: Boolean get() = board.all { it != 0 }
        val isCorrect: Boolean get() = board.contentEquals(solution)
    }
}
