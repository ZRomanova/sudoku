package com.zoya.sudoku.engine

import kotlin.random.Random

/**
 * "Dig holes": starting from a full solution, tries clearing cells in random order, keeping each
 * removal only if the puzzle still provably has exactly one solution, until [minGivens] is
 * reached (or no more cells can be safely removed).
 *
 * Uniqueness is proven one of two ways:
 * - Logic: [DifficultyGrader] finishes the puzzle using techniques up to [maxTechnique]. Every
 *   grader technique is a sound deduction, so a logic-only completion is necessarily the one and
 *   only solution - and a person can reach it without guessing. This is cheap and bounded.
 * - Search (only when [allowGuessing]): a backtracking count finds exactly one solution within
 *   [searchNodeLimit]. Running out of budget is NOT treated as proof - the cell is simply kept -
 *   so an unusual region shape can make the puzzle slightly easier but never crash generation
 *   or let a non-unique puzzle through.
 */
object PuzzleCarver {
    const val DEFAULT_SEARCH_NODE_LIMIT = 100_000

    fun carve(
        solution: IntArray,
        units: Units,
        rng: Random,
        minGivens: Int,
        maxTechnique: Technique = Technique.entries.last(),
        allowGuessing: Boolean = false,
        grader: DifficultyGrader = DifficultyGrader(units),
        searchNodeLimit: Int = DEFAULT_SEARCH_NODE_LIMIT
    ): IntArray {
        val searcher = SudokuSolver(units, nodeLimit = searchNodeLimit)
        val puzzle = solution.copyOf()
        val order = (0 until BOARD_SIZE).shuffled(rng)
        var givenCount = BOARD_SIZE
        for (cell in order) {
            if (givenCount <= minGivens) break
            val saved = puzzle[cell]
            if (saved == 0) continue
            puzzle[cell] = 0
            if (provablyUnique(puzzle, solution, maxTechnique, allowGuessing, grader, searcher)) {
                givenCount--
            } else {
                puzzle[cell] = saved
            }
        }
        return puzzle
    }

    private fun provablyUnique(
        puzzle: IntArray,
        solution: IntArray,
        maxTechnique: Technique,
        allowGuessing: Boolean,
        grader: DifficultyGrader,
        searcher: SudokuSolver
    ): Boolean {
        val logic = grader.solveLogically(puzzle, maxTechnique)
        if (logic.grade.solvedCompletely && logic.board.contentEquals(solution)) return true
        if (!allowGuessing) return false
        return try {
            searcher.countSolutions(puzzle, limit = 2) == 1
        } catch (e: SolverBudgetExceeded) {
            false
        }
    }
}
