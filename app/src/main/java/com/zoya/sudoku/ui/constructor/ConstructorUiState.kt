package com.zoya.sudoku.ui.constructor

import com.zoya.sudoku.engine.BOARD_SIZE
import com.zoya.sudoku.ui.components.ERASER

const val UNCOLORED = -1

/** activeTool value once a color hits 9/9 mid-paint - nothing is selected, painting is a no-op
 *  until the user explicitly picks another color or the eraser. */
const val NO_TOOL = -2

data class ConstructorUiState(
    val cellColors: List<Int> = List(BOARD_SIZE) { UNCOLORED },
    /** Cached full valid Sudoku solution consistent with every cell painted so far. Null only in
     *  the brief pre-seed window right after the screen opens. List, not IntArray, so the data
     *  class's structural equals/copy - relied on by the staleness guard in tapCell and by
     *  Compose's collectAsState - actually compares content instead of reference. */
    val witness: List<Int>? = null,
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

    /**
     * Cheap, synchronous, no-solver hint: uncolored cells whose witness digit is already used
     * among cells painted with the active tool - i.e. tapping them right now would conflict with
     * the cached witness and trigger the rarer regenerate-or-reject path in the ViewModel. This is
     * advisory only - such cells stay tappable, they are never hard-blocked.
     */
    val hintedCells: Set<Int>
        get() {
            val w = witness ?: return emptySet()
            val tool = activeTool
            if (tool == ERASER || tool == NO_TOOL) return emptySet()
            val usedDigits = BooleanArray(10)
            for (i in cellColors.indices) if (cellColors[i] == tool) usedDigits[w[i]] = true
            return cellColors.indices.filterTo(mutableSetOf()) { i -> cellColors[i] == UNCOLORED && usedDigits[w[i]] }
        }
}
