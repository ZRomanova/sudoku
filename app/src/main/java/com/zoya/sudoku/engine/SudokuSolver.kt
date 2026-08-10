package com.zoya.sudoku.engine

import kotlin.random.Random

class SolverBudgetExceeded(message: String) : Exception(message)

private const val FULL_MASK = 0b1_1111_1111 // digits 1..9, bit (d-1)

/**
 * Generic backtracking solver over whatever 27 units [Units] precomputed - rows/columns are always
 * standard, regions can be any shape. Bitmask candidate propagation + MRV cell ordering keeps 9x9
 * solves fast regardless of region geometry.
 */
class SudokuSolver(private val units: Units, private val nodeLimit: Int = 2_000_000) {

    /** First solution found, or null if [givens] is contradictory or unsolvable. */
    fun solve(givens: IntArray, randomize: Boolean = false, rng: Random = Random): IntArray? {
        val board = givens.copyOf()
        val candidates = initialCandidates(board) ?: return null
        var nodes = 0

        fun backtrack(): Boolean {
            nodes++
            if (nodes > nodeLimit) throw SolverBudgetExceeded("solve() exceeded $nodeLimit nodes")
            val cell = selectCellMRV(board, candidates) ?: return true
            val digits = digitsFromMask(candidates[cell], randomize, rng)
            for (d in digits) {
                val cleared = place(board, candidates, cell, d)
                if (backtrack()) return true
                undo(board, candidates, cell, d, cleared)
            }
            return false
        }

        return if (backtrack()) board else null
    }

    /** Counts solutions, stopping as soon as [limit] is reached - used for uniqueness checks. */
    fun countSolutions(givens: IntArray, limit: Int = 2): Int {
        val board = givens.copyOf()
        val candidates = initialCandidates(board) ?: return 0
        var count = 0
        var nodes = 0

        fun backtrack() {
            if (count >= limit) return
            nodes++
            if (nodes > nodeLimit) throw SolverBudgetExceeded("countSolutions() exceeded $nodeLimit nodes")
            val cell = selectCellMRV(board, candidates)
            if (cell == null) {
                count++
                return
            }
            var mask = candidates[cell]
            while (mask != 0 && count < limit) {
                val bit = mask and (-mask)
                val d = Integer.numberOfTrailingZeros(bit) + 1
                mask = mask xor bit
                val cleared = place(board, candidates, cell, d)
                backtrack()
                undo(board, candidates, cell, d, cleared)
            }
        }

        backtrack()
        return count
    }

    private fun hasGivenConflict(board: IntArray): Boolean {
        for (unit in units.units) {
            var seen = 0
            for (cell in unit) {
                val d = board[cell]
                if (d == 0) continue
                val bit = 1 shl (d - 1)
                if (seen and bit != 0) return true
                seen = seen or bit
            }
        }
        return false
    }

    private fun initialCandidates(board: IntArray): IntArray? {
        if (hasGivenConflict(board)) return null
        val candidates = IntArray(BOARD_SIZE) { FULL_MASK }
        for (cell in 0 until BOARD_SIZE) {
            if (board[cell] != 0) continue
            var mask = FULL_MASK
            for (peer in units.peers[cell]) {
                val d = board[peer]
                if (d != 0) mask = mask and (1 shl (d - 1)).inv()
            }
            candidates[cell] = mask
        }
        return candidates
    }

    private fun selectCellMRV(board: IntArray, candidates: IntArray): Int? {
        var best = -1
        var bestCount = Int.MAX_VALUE
        for (cell in 0 until BOARD_SIZE) {
            if (board[cell] != 0) continue
            val count = Integer.bitCount(candidates[cell])
            if (count < bestCount) {
                bestCount = count
                best = cell
                if (count == 0) return best
            }
        }
        return if (best == -1) null else best
    }

    private fun place(board: IntArray, candidates: IntArray, cell: Int, d: Int): IntArray {
        board[cell] = d
        val bit = 1 shl (d - 1)
        val cleared = ArrayList<Int>()
        for (peer in units.peers[cell]) {
            if (board[peer] == 0 && candidates[peer] and bit != 0) {
                candidates[peer] = candidates[peer] and bit.inv()
                cleared.add(peer)
            }
        }
        return cleared.toIntArray()
    }

    private fun undo(board: IntArray, candidates: IntArray, cell: Int, d: Int, cleared: IntArray) {
        board[cell] = 0
        val bit = 1 shl (d - 1)
        for (peer in cleared) {
            candidates[peer] = candidates[peer] or bit
        }
    }

    private fun digitsFromMask(mask: Int, randomize: Boolean, rng: Random): IntArray {
        val digits = ArrayList<Int>(9)
        var m = mask
        while (m != 0) {
            val bit = m and (-m)
            digits.add(Integer.numberOfTrailingZeros(bit) + 1)
            m = m xor bit
        }
        if (randomize) digits.shuffle(rng)
        return digits.toIntArray()
    }
}
