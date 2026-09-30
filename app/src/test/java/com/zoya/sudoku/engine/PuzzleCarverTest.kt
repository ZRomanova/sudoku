package com.zoya.sudoku.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleCarverTest {

    @Test
    fun `carved puzzle is logic-solvable, uniquely solvable and a subset of its solution`() {
        val layouts = listOf(RegionLayout.classicBoxes(), diagonalJigsaw())
        for (layout in layouts) {
            val units = Units(layout)
            val solver = SudokuSolver(units)
            val solution = solver.solve(IntArray(BOARD_SIZE), randomize = true, rng = Random(1))!!
            val puzzle = PuzzleCarver.carve(solution, units, Random(2), minGivens = 24)

            assertEquals(1, solver.countSolutions(puzzle, limit = 2))
            assertTrue(DifficultyGrader(units).gradePuzzle(puzzle).solvedCompletely)
            for (cell in 0 until BOARD_SIZE) {
                val given = puzzle[cell]
                assertTrue(given == 0 || given == solution[cell])
            }
            assertTrue(puzzle.count { it != 0 } <= solution.count { it != 0 })
            assertTrue(puzzle.count { it != 0 } >= 24)
        }
    }

    @Test
    fun `singles-only carve never needs anything beyond hidden singles`() {
        val units = Units(RegionLayout.classicBoxes())
        val solution = SudokuSolver(units).solve(IntArray(BOARD_SIZE), randomize = true, rng = Random(3))!!
        val puzzle = PuzzleCarver.carve(solution, units, Random(4), minGivens = 20, maxTechnique = Technique.HIDDEN_SINGLE)

        val grade = DifficultyGrader(units).gradePuzzle(puzzle)
        assertTrue(grade.solvedCompletely)
        assertTrue(grade.maxTechnique!! <= Technique.HIDDEN_SINGLE)
    }
}
