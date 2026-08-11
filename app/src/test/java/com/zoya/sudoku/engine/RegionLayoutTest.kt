package com.zoya.sudoku.engine

import org.junit.Assert.assertFalse
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

    @Test
    fun `wouldRemainFeasible never blocks the very first paint on an empty board`() {
        val colors = List(BOARD_SIZE) { -1 }
        for (color in 0..8) {
            assertTrue("color $color falsely blocked on an empty board", wouldRemainFeasible(colors, cell = 0, color = color))
        }
    }

    @Test
    fun `wouldRemainFeasible has no false positives on a classic-box-compatible partial layout`() {
        // Colors 0..6 filled in as classic 3x3 boxes (63 cells); boxes 7 and 8 left uncolored.
        // Since the classic layout is always completable, no candidate cell/color pair here
        // should ever be flagged as a proven dead end.
        val classic = RegionLayout.classicBoxes().cellRegion
        val colors = MutableList(BOARD_SIZE) { -1 }
        for (cell in 0 until BOARD_SIZE) {
            val box = classic[cell]
            if (box <= 6) colors[cell] = box
        }
        for (cell in colors.indices) {
            if (colors[cell] != -1) continue
            for (color in 7..8) {
                assertTrue(
                    "cell $cell falsely blocked for color $color",
                    wouldRemainFeasible(colors, cell, color)
                )
            }
        }
    }

    @Test
    fun `wouldRemainFeasible rejects a region already past 9 members`() {
        // Not reachable through the Constructor UI (the 9-cap is enforced before this is ever
        // called), but the solver-level check should still recognize a 10-cell region as
        // impossible on its own merits - a region can only hold 9 distinct digits.
        val colors = MutableList(BOARD_SIZE) { -1 }
        for (c in 0 until 9) colors[c] = 0 // all of row 0
        assertFalse(wouldRemainFeasible(colors, cell = 9, color = 0, nodeLimit = 2_000_000))
    }
}
