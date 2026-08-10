package com.zoya.sudoku.ui.constructor

import com.zoya.sudoku.engine.BOARD_SIZE

const val UNCOLORED = -1

data class ConstructorUiState(
    val cellColors: List<Int> = List(BOARD_SIZE) { UNCOLORED },
    val activeTool: Int = 0,
    val name: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
) {
    val counts: IntArray
        get() = IntArray(9).also { arr -> cellColors.forEach { c -> if (c in 0..8) arr[c]++ } }

    val isComplete: Boolean
        get() = counts.all { it == 9 }

    val canSave: Boolean
        get() = isComplete && name.isNotBlank() && !isSaving
}
