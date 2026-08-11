package com.zoya.sudoku.ui.constructor

import com.zoya.sudoku.engine.BOARD_SIZE

const val UNCOLORED = -1

/** activeTool value once a color hits 9/9 mid-paint - nothing is selected, painting is a no-op
 *  until the user explicitly picks another color or the eraser. */
const val NO_TOOL = -2

data class ConstructorUiState(
    val cellColors: List<Int> = List(BOARD_SIZE) { UNCOLORED },
    val activeTool: Int = 0,
    val blockedCells: Set<Int> = emptySet(),
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
