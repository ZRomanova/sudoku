package com.zoya.sudoku.engine

import kotlin.random.Random

/**
 * "Dig holes": starting from a full solution, tries clearing cells in random order, keeping each
 * removal only if the puzzle still has exactly one solution, until [minGivens] is reached (or no
 * more cells can be safely removed). Stopping at a difficulty-appropriate given-count rather than
 * carving all the way to the theoretical minimum matters for performance: the last few removals
 * toward an absolute-minimum puzzle are by far the most expensive uniqueness checks, especially on
 * region shapes that give the solver little pruning power (e.g. regions with only one cell per
 * row/column), so this bounds worst-case cost instead of always paying for a maximally-carved
 * puzzle no difficulty tier actually needs.
 */
object PuzzleCarver {
    fun carve(solution: IntArray, units: Units, rng: Random, minGivens: Int): IntArray {
        val solver = SudokuSolver(units)
        val puzzle = solution.copyOf()
        val order = (0 until BOARD_SIZE).shuffled(rng)
        var givenCount = BOARD_SIZE
        for (cell in order) {
            if (givenCount <= minGivens) break
            val saved = puzzle[cell]
            if (saved == 0) continue
            puzzle[cell] = 0
            if (solver.countSolutions(puzzle, limit = 2) != 1) {
                puzzle[cell] = saved
            } else {
                givenCount--
            }
        }
        return puzzle
    }
}
