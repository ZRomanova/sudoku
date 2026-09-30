package com.zoya.sudoku.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression for generation crashing on unusual region shapes: a single long randomized search
 * for the solution grid used to blow its node budget on roughly a quarter of scrambled layouts.
 */
class PuzzleGeneratorStressTest {

    /** Classic boxes with [swaps] random swaps of neighbouring cells' regions - odd, non-box shapes. */
    private fun scrambledLayout(rng: Random, swaps: Int): RegionLayout? {
        val cells = RegionLayout.classicBoxes().cellRegion.copyOf()
        repeat(swaps) {
            val a = rng.nextInt(BOARD_SIZE)
            val neighbours = listOf(a - 1, a + 1, a - GRID_DIM, a + GRID_DIM)
                .filter { it in 0 until BOARD_SIZE && (rowOf(it) == rowOf(a) || colOf(it) == colOf(a)) }
            val b = neighbours.random(rng)
            cells[a] = cells[b].also { cells[b] = cells[a] }
        }
        return RegionLayout(cells).takeIf { it.isSolvable() }
    }

    @Test
    fun `every tier yields a uniquely solvable puzzle on scrambled layouts`() {
        val rng = Random(42)
        val layouts = generateSequence(0) { it + 1 }
            .mapNotNull { scrambledLayout(rng, 10 + rng.nextInt(60)) }
            .take(5)
            .toList()

        for (layout in layouts) {
            val units = Units(layout)
            val generator = PuzzleGenerator(units)
            val grader = DifficultyGrader(units)
            for (difficulty in Difficulty.entries) {
                val puzzle = generator.generatePuzzle(difficulty, Random(difficulty.ordinal * 7L))

                assertTrue(isValidCompleteSolution(puzzle.solution, units))
                for (cell in 0 until BOARD_SIZE) {
                    assertTrue(puzzle.givens[cell] == 0 || puzzle.givens[cell] == puzzle.solution[cell])
                }
                if (!DifficultyConfig.allowsGuessing(difficulty)) {
                    val grade = grader.gradePuzzle(puzzle.givens, DifficultyConfig.maxTechnique(difficulty))
                    assertTrue("$difficulty must be solvable without guessing", grade.solvedCompletely)
                }
                assertEquals(1, SudokuSolver(units, nodeLimit = 50_000_000).countSolutions(puzzle.givens, limit = 2))
            }
        }
    }
}
