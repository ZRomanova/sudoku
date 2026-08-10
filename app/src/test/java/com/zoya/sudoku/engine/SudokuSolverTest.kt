package com.zoya.sudoku.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuSolverTest {

    @Test
    fun `solve honors all units on classic boxes`() {
        val units = Units(RegionLayout.classicBoxes())
        val solution = SudokuSolver(units).solve(IntArray(BOARD_SIZE))
        assertNotNull(solution)
        assertTrue(isValidCompleteSolution(solution!!, units))
    }

    @Test
    fun `solve honors all units on non-square jigsaw regions`() {
        val units = Units(diagonalJigsaw())
        val solution = SudokuSolver(units).solve(IntArray(BOARD_SIZE))
        assertNotNull(solution)
        assertTrue(isValidCompleteSolution(solution!!, units))
    }

    @Test
    fun `randomized solves stay valid and are not always identical`() {
        val units = Units(RegionLayout.classicBoxes())
        val solver = SudokuSolver(units)
        val solutions = (0 until 8).map { seed ->
            solver.solve(IntArray(BOARD_SIZE), randomize = true, rng = Random(seed))
        }
        solutions.forEach { s ->
            assertNotNull(s)
            assertTrue(isValidCompleteSolution(s!!, units))
        }
        assertTrue(
            "randomized solves should not all collapse to the same grid",
            solutions.distinctBy { it!!.toList() }.size > 1
        )
    }

    @Test
    fun `countSolutions returns exactly 1 for a known-unique puzzle`() {
        val units = Units(RegionLayout.classicBoxes())
        val solver = SudokuSolver(units)
        val solution = solver.solve(IntArray(BOARD_SIZE))!!
        val puzzle = PuzzleCarver.carve(solution, units, Random(42), minGivens = 24)
        assertEquals(1, solver.countSolutions(puzzle, limit = 2))
    }

    @Test
    fun `countSolutions caps quickly at the limit for an empty board`() {
        val units = Units(RegionLayout.classicBoxes())
        val solver = SudokuSolver(units)
        assertEquals(2, solver.countSolutions(IntArray(BOARD_SIZE), limit = 2))
    }

    @Test
    fun `contradictory givens return null and zero solutions`() {
        val units = Units(RegionLayout.classicBoxes())
        val solver = SudokuSolver(units)
        val givens = IntArray(BOARD_SIZE)
        givens[0] = 5
        givens[1] = 5
        assertNull(solver.solve(givens))
        assertEquals(0, solver.countSolutions(givens))
    }
}
