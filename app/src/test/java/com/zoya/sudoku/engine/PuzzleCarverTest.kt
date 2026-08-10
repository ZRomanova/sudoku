package com.zoya.sudoku.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleCarverTest {

    @Test
    fun `carved puzzle stays uniquely solvable and is a subset of its solution`() {
        val layouts = listOf(RegionLayout.classicBoxes(), diagonalJigsaw())
        for (layout in layouts) {
            val units = Units(layout)
            val solver = SudokuSolver(units)
            val solution = solver.solve(IntArray(BOARD_SIZE), randomize = true, rng = Random(1))!!
            val puzzle = PuzzleCarver.carve(solution, units, Random(2), minGivens = 24)

            assertEquals(1, solver.countSolutions(puzzle, limit = 2))
            for (cell in 0 until BOARD_SIZE) {
                val given = puzzle[cell]
                assertTrue(given == 0 || given == solution[cell])
            }
            assertTrue(puzzle.count { it != 0 } <= solution.count { it != 0 })
            assertTrue(puzzle.count { it != 0 } >= 24)
        }
    }
}
