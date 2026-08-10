package com.zoya.sudoku.ui.game

sealed interface GameUiState {
    data object Loading : GameUiState

    data class Loaded(
        val cellRegion: IntArray,
        val board: IntArray,
        val solution: IntArray,
        val givens: IntArray,
        val selectedCell: Int?,
        val showErrors: Boolean
    ) : GameUiState {
        val isFull: Boolean get() = board.all { it != 0 }
    }
}
