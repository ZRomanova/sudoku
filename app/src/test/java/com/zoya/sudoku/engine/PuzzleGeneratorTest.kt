package com.zoya.sudoku.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleGeneratorTest {

    @Test
    fun `generates an internally consistent puzzle for every difficulty on classic boxes`() {
        val units = Units(RegionLayout.classicBoxes())
        val generator = PuzzleGenerator(units)
        val solver = SudokuSolver(units)

        for (difficulty in Difficulty.entries) {
            val puzzle = generator.generatePuzzle(difficulty, Random(difficulty.ordinal + 100))

            assertTrue(isValidCompleteSolution(puzzle.solution, units))
            for (cell in 0 until BOARD_SIZE) {
                val given = puzzle.givens[cell]
                assertTrue(given == 0 || given == puzzle.solution[cell])
            }
            assertEquals(1, solver.countSolutions(puzzle.givens, limit = 2))
        }
    }

    @Test
    fun `generates an internally consistent puzzle on non-square jigsaw regions too`() {
        val units = Units(diagonalJigsaw())
        val generator = PuzzleGenerator(units)
        val solver = SudokuSolver(units)

        val puzzle = generator.generatePuzzle(Difficulty.MEDIUM, Random(7))

        assertTrue(isValidCompleteSolution(puzzle.solution, units))
        assertEquals(1, solver.countSolutions(puzzle.givens, limit = 2))
    }

    @Test
    fun `two Play taps on the same layout never produce the same puzzle`() {
        val units = Units(RegionLayout.classicBoxes())
        val generator = PuzzleGenerator(units)

        val a = generator.generatePuzzle(Difficulty.EASY, Random(1))
        val b = generator.generatePuzzle(Difficulty.EASY, Random(2))

        assertTrue(!a.solution.contentEquals(b.solution) || !a.givens.contentEquals(b.givens))
    }
}
