package com.zoya.sudoku.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyGraderTest {

    @Test
    fun `grading is deterministic for a fixed board`() {
        val units = Units(RegionLayout.classicBoxes())
        val grader = DifficultyGrader(units)
        val solution = SudokuSolver(units).solve(IntArray(BOARD_SIZE))!!
        val puzzle = solution.copyOf().also {
            it[5] = 0
            it[40] = 0
        }

        assertEquals(grader.gradePuzzle(puzzle), grader.gradePuzzle(puzzle))
    }

    @Test
    fun `four independent missing cells resolve as naked singles - easy grade`() {
        val units = Units(RegionLayout.classicBoxes())
        val solution = SudokuSolver(units).solve(IntArray(BOARD_SIZE))!!
        val puzzle = solution.copyOf()
        // Distinct rows, distinct cols, distinct 3x3 boxes -> each cleared cell's peers all stay
        // given, so each is directly a naked single, independent of the others.
        for ((r, c) in listOf(0 to 0, 3 to 4, 6 to 8, 4 to 1)) {
            puzzle[r * GRID_DIM + c] = 0
        }

        val grade = DifficultyGrader(units).gradePuzzle(puzzle)

        assertTrue(grade.solvedCompletely)
        assertFalse(grade.requiresGuessing)
        assertEquals(Technique.NAKED_SINGLE, grade.maxTechnique)
        assertEquals(77, grade.givenCount)
    }

    @Test
    fun `a completely empty board cannot be resolved without guessing`() {
        val units = Units(RegionLayout.classicBoxes())
        val grade = DifficultyGrader(units).gradePuzzle(IntArray(BOARD_SIZE))

        assertTrue(grade.requiresGuessing)
        assertFalse(grade.solvedCompletely)
        assertNull(grade.maxTechnique)
        assertEquals(0, grade.givenCount)
    }
}
