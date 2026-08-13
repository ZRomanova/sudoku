package com.zoya.sudoku.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
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

    @Test
    fun `regenerateWitness never fails the very first paint on an empty board`() {
        val colors = List(BOARD_SIZE) { -1 }
        for (color in 0..8) {
            assertNotNull("color $color falsely rejected on an empty board", regenerateWitness(colors, cell = 0, color = color))
        }
    }

    @Test
    fun `regenerateWitness has no false negatives on a classic-box-compatible partial layout`() {
        // Colors 0..6 filled in as classic 3x3 boxes (63 cells); boxes 7 and 8 left uncolored.
        // Since the classic layout is always completable, no candidate cell/color pair here
        // should ever be flagged as impossible.
        val classic = RegionLayout.classicBoxes().cellRegion
        val colors = MutableList(BOARD_SIZE) { -1 }
        for (cell in 0 until BOARD_SIZE) {
            val box = classic[cell]
            if (box <= 6) colors[cell] = box
        }
        for (cell in colors.indices) {
            if (colors[cell] != -1) continue
            for (color in 7..8) {
                assertNotNull(
                    "cell $cell falsely rejected for color $color",
                    regenerateWitness(colors, cell, color)
                )
            }
        }
    }

    @Test
    fun `regenerateWitness rejects a region already past 9 members`() {
        // Not reachable through the Constructor UI (the 9-cap is enforced before this is ever
        // called), but the solver-level check should still recognize a 10-cell region as
        // impossible on its own merits - a region can only hold 9 distinct digits.
        val colors = MutableList(BOARD_SIZE) { -1 }
        for (c in 0 until 9) colors[c] = 0 // all of row 0
        assertNull(regenerateWitness(colors, cell = 9, color = 0, nodeLimit = 2_000_000))
    }

    @Test
    fun `regenerateWitness finds an alternate grid when the cached witness would duplicate a digit in the region`() {
        val units = Units(RegionLayout.classicBoxes())
        val seed = SudokuSolver(units).solve(IntArray(BOARD_SIZE))!!
        val cellA = 0
        val digit = seed[cellA]
        val cellB = (1 until BOARD_SIZE).first { seed[it] == digit } // exists: digit appears 9x
        val colors = MutableList(BOARD_SIZE) { -1 }
        colors[cellA] = 5
        assertEquals(digit, seed[cellB]) // sanity: the seed grid itself would conflict if cellB joined region 5

        val alt = regenerateWitness(colors, cellB, 5)

        assertNotNull(alt)
        assertNotEquals(alt!![cellA], alt[cellB]) // the *found* grid actually resolves the conflict
        // alt is only guaranteed to honor rows/columns (always-present units) and the two decided
        // region-5 cells - not classicBoxes' box constraint, since it wasn't solved against it.
        assertTrue(units.units.take(GRID_DIM * 2).all { unit -> unit.map { alt[it] }.toSet().size == GRID_DIM })
    }
}
