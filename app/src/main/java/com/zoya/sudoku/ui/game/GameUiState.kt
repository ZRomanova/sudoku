package com.zoya.sudoku.ui.game

import com.zoya.sudoku.engine.Difficulty

sealed interface GameUiState {
    data object Loading : GameUiState

    data class Loaded(
        val layoutId: Long,
        val layoutName: String,
        val difficulty: Difficulty,
        val cellRegion: IntArray,
        val board: IntArray,
        val solution: IntArray,
        val givens: IntArray,
        /** Bit (digit-1) set means that digit is pencilled in as a candidate for the cell. */
        val notes: IntArray,
        val notesMode: Boolean,
        val selectedCell: Int?,
        val showErrors: Boolean
    ) : GameUiState {
        val isFull: Boolean get() = board.all { it != 0 }
        val isCorrect: Boolean get() = board.contentEquals(solution)
    }
}
