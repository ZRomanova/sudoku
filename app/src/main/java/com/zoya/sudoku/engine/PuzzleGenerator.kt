package com.zoya.sudoku.engine

import kotlin.math.abs
import kotlin.random.Random

enum class Difficulty { EASY, MEDIUM, HARD }

data class Puzzle(
    val solution: IntArray,
    val givens: IntArray,
    val difficulty: Difficulty
)

/** Tunable thresholds, calibrated empirically rather than derived analytically. */
object DifficultyConfig {
    const val EASY_MIN_GIVENS = 36
    const val MEDIUM_MIN_GIVENS = 28
    const val MAX_GENERATION_ATTEMPTS = 20

    /** Carve target per difficulty - stop removing cells once this given-count is reached. */
    fun targetGivens(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EASY -> 42
        Difficulty.MEDIUM -> 30
        Difficulty.HARD -> 24
    }
}

/**
 * Produces a brand-new solution + given-set every time it's invoked (never repeats a previous
 * puzzle) for a fixed region layout, targeting one of 3 difficulty tiers.
 */
class PuzzleGenerator(private val units: Units) {
    private val solver = SudokuSolver(units)
    private val grader = DifficultyGrader(units)

    fun generatePuzzle(targetDifficulty: Difficulty, rng: Random): Puzzle {
        val solution = solver.solve(IntArray(BOARD_SIZE), randomize = true, rng = rng)
            ?: throw IllegalStateException("Region layout has no valid sudoku solution")

        var best: Pair<IntArray, Difficulty>? = null
        repeat(DifficultyConfig.MAX_GENERATION_ATTEMPTS) {
            val minGivens = DifficultyConfig.targetGivens(targetDifficulty)
            val puzzle = PuzzleCarver.carve(solution, units, rng, minGivens)
            val tier = classify(grader.gradePuzzle(puzzle))
            if (tier == targetDifficulty) {
                return Puzzle(solution, puzzle, tier)
            }
            if (best == null || isCloser(targetDifficulty, tier, best!!.second)) {
                best = puzzle to tier
            }
        }
        val fallback = requireNotNull(best)
        return Puzzle(solution, fallback.first, fallback.second)
    }

    private fun classify(grade: Grade): Difficulty {
        if (grade.requiresGuessing) return Difficulty.HARD
        val easyTechnique = grade.maxTechnique == null ||
            grade.maxTechnique == Technique.NAKED_SINGLE ||
            grade.maxTechnique == Technique.HIDDEN_SINGLE
        return when {
            easyTechnique && grade.givenCount >= DifficultyConfig.EASY_MIN_GIVENS -> Difficulty.EASY
            grade.givenCount >= DifficultyConfig.MEDIUM_MIN_GIVENS -> Difficulty.MEDIUM
            else -> Difficulty.HARD
        }
    }

    private fun isCloser(target: Difficulty, candidate: Difficulty, currentBest: Difficulty): Boolean {
        return abs(candidate.ordinal - target.ordinal) < abs(currentBest.ordinal - target.ordinal)
    }
}
