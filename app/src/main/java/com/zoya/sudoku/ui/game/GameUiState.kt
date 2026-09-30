package com.zoya.sudoku.ui.game

import com.zoya.sudoku.engine.BOARD_SIZE
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
        val showErrors: Boolean,
        /** A wrong "Завершить" already logged a loss for this game. */
        val attemptFailed: Boolean = false,
        /** Finished correctly - the board is read-only and shown under the success banner. */
        val isSolved: Boolean = false
    ) : GameUiState {
        val isFull: Boolean get() = board.all { it != 0 }

        /** Every puzzle has exactly one solution, so a full board is right iff it matches it. */
        val isCorrect: Boolean get() = board.contentEquals(solution)

        /** Filled cells whose digit differs from the solution. */
        val wrongCells: Set<Int>
            get() = (0 until BOARD_SIZE).filterTo(HashSet()) { board[it] != 0 && board[it] != solution[it] }
    }
}
