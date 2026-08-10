package com.zoya.sudoku.engine

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionLayoutTest {

    @Test
    fun `classic 3x3 boxes are solvable`() {
        assertTrue(RegionLayout.classicBoxes().isSolvable())
    }

    @Test
    fun `non-square diagonal jigsaw regions are solvable`() {
        assertTrue(diagonalJigsaw().isSolvable())
        val units = Units(diagonalJigsaw())
        assertTrue(isValidCompleteSolution(diagonalJigsawSolution, units))
    }

    @Test
    fun `contradictory givens in the same row are unsolvable`() {
        val units = Units(RegionLayout.classicBoxes())
        val givens = IntArray(BOARD_SIZE)
        givens[0] = 5 // row 0, col 0
        givens[1] = 5 // row 0, col 1 - same row, same digit
        assertNull(SudokuSolver(units).solve(givens))
    }

    @Test
    fun `contradictory givens in the same region are unsolvable`() {
        val units = Units(RegionLayout.classicBoxes())
        val givens = IntArray(BOARD_SIZE)
        givens[0] = 7  // box 0
        givens[10] = 7 // cell (1,1), same box, same digit
        assertNull(SudokuSolver(units).solve(givens))
    }
}
