package com.zoya.sudoku.engine

import kotlin.random.Random

enum class Difficulty { EASY, MEDIUM, HARD }

data class Puzzle(
    val solution: IntArray,
    val givens: IntArray,
    val difficulty: Difficulty
)

/** Generation could not produce a puzzle (e.g. the layout has no valid Sudoku at all). */
class PuzzleGenerationException(message: String) : Exception(message)

/** Tunable thresholds, calibrated empirically rather than derived analytically. */
object DifficultyConfig {
    const val MAX_GENERATION_ATTEMPTS = 8

    /**
     * Randomized backtracking for a full grid is heavy-tailed: almost every run finishes in a few
     * hundred nodes, but an unlucky early choice on an unusual region shape can wander for
     * millions. Many short restarts beat one long run by orders of magnitude.
     */
    const val SOLUTION_RESTART_NODE_LIMIT = 20_000
    const val SOLUTION_MAX_RESTARTS = 200

    /** Carve target per difficulty - stop removing cells once this given-count is reached. */
    fun targetGivens(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EASY -> 40
        Difficulty.MEDIUM -> 30
        Difficulty.HARD -> 24
    }

    /** Hardest technique a player may need for tiers solved without guessing. */
    fun maxTechnique(difficulty: Difficulty): Technique = when (difficulty) {
        Difficulty.EASY -> Technique.HIDDEN_SINGLE
        Difficulty.MEDIUM -> Technique.LOCKED_CANDIDATES
        Difficulty.HARD -> Technique.NAKED_PAIR
    }

    /** Only the hardest tier may need guessing; its uniqueness is then proven by search instead. */
    fun allowsGuessing(difficulty: Difficulty): Boolean = difficulty == Difficulty.HARD
}

/**
 * Produces a brand-new solution + given-set every time it's invoked (never repeats a previous
 * puzzle) for a fixed region layout, targeting one of 3 difficulty tiers. Every puzzle it returns
 * has exactly one solution (see [PuzzleCarver]); EASY and MEDIUM are also solvable by logic alone.
 */
class PuzzleGenerator(private val units: Units) {
    private val grader = DifficultyGrader(units)

    fun generatePuzzle(targetDifficulty: Difficulty, rng: Random): Puzzle {
        val solution = randomSolution(rng)
        val maxTechnique = DifficultyConfig.maxTechnique(targetDifficulty)
        val allowGuessing = DifficultyConfig.allowsGuessing(targetDifficulty)
        val targetGivens = DifficultyConfig.targetGivens(targetDifficulty)

        // Carving order is random, so repeat attempts land on different given-counts; keep the
        // one closest to the tier's intent in case none hits the target outright.
        var best: IntArray? = null
        var bestScore = Int.MIN_VALUE
        repeat(DifficultyConfig.MAX_GENERATION_ATTEMPTS) {
            val puzzle = PuzzleCarver.carve(solution, units, rng, targetGivens, maxTechnique, allowGuessing, grader)
            val grade = grader.gradePuzzle(puzzle, maxTechnique)
            check(allowGuessing || grade.solvedCompletely) { "Carver returned a puzzle logic can't finish" }
            val usesTierTechnique = targetDifficulty == Difficulty.EASY || grade.requiresGuessing ||
                (grade.maxTechnique != null && grade.maxTechnique >= Technique.LOCKED_CANDIDATES)
            if (grade.givenCount <= targetGivens && usesTierTechnique) {
                return Puzzle(solution, puzzle, targetDifficulty)
            }
            val score = -grade.givenCount + (if (usesTierTechnique) 100 else 0)
            if (score > bestScore) {
                bestScore = score
                best = puzzle
            }
        }
        return Puzzle(solution, requireNotNull(best), targetDifficulty)
    }

    private fun randomSolution(rng: Random): IntArray {
        val solver = SudokuSolver(units, nodeLimit = DifficultyConfig.SOLUTION_RESTART_NODE_LIMIT)
        repeat(DifficultyConfig.SOLUTION_MAX_RESTARTS) {
            val found = try {
                solver.solve(IntArray(BOARD_SIZE), randomize = true, rng = rng)
            } catch (e: SolverBudgetExceeded) {
                return@repeat
            }
            // A search that ran to completion without finding anything is a proof, not bad luck.
            return found ?: throw PuzzleGenerationException("Region layout has no valid sudoku solution")
        }
        throw PuzzleGenerationException("Couldn't build a solution grid for this region layout")
    }
}
