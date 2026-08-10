package com.zoya.sudoku.engine

/** Ladder of solving techniques, cheapest/most-obvious first. Ordinal order == difficulty order. */
enum class Technique { NAKED_SINGLE, HIDDEN_SINGLE, LOCKED_CANDIDATES, NAKED_PAIR }

data class Grade(
    val maxTechnique: Technique?,
    val requiresGuessing: Boolean,
    val solvedCompletely: Boolean,
    val givenCount: Int
)

/**
 * Simulates a human solving the puzzle using only [Technique]s, always applying the cheapest
 * applicable one first. Whatever it can't resolve this way is treated as "requires guessing" -
 * the actual uniqueness of the puzzle is already guaranteed by [PuzzleCarver], this grader only
 * measures how hard it is to get there without backtracking search.
 *
 * Classic "pointing pairs" / "box-line reduction" assume a box spans exactly 3 rows x 3 columns,
 * which doesn't hold for arbitrary regions. [LOCKED_CANDIDATES] here is the shape-agnostic
 * generalization: for any two units A, B, if all of digit d's remaining candidates in A lie
 * within A intersect B, d can be eliminated from B outside that intersection.
 */
class DifficultyGrader(private val units: Units) {

    fun gradePuzzle(givens: IntArray): Grade {
        val board = givens.copyOf()
        val candidates = buildCandidates(board)
        var maxTechnique: Technique? = null
        val givenCount = givens.count { it != 0 }

        while (true) {
            if (board.all { it != 0 }) {
                return Grade(maxTechnique, requiresGuessing = false, solvedCompletely = true, givenCount = givenCount)
            }
            val applied = applyNakedSingle(board, candidates)
                ?: applyHiddenSingle(board, candidates)
                ?: applyLockedCandidates(board, candidates)
                ?: applyNakedPair(board, candidates)

            if (applied == null) {
                return Grade(maxTechnique, requiresGuessing = true, solvedCompletely = false, givenCount = givenCount)
            }
            if (maxTechnique == null || applied.ordinal > maxTechnique.ordinal) {
                maxTechnique = applied
            }
        }
    }

    private fun buildCandidates(board: IntArray): IntArray {
        val candidates = IntArray(BOARD_SIZE) { 0b1_1111_1111 }
        for (cell in 0 until BOARD_SIZE) {
            if (board[cell] != 0) continue
            var mask = 0b1_1111_1111
            for (peer in units.peers[cell]) {
                val d = board[peer]
                if (d != 0) mask = mask and (1 shl (d - 1)).inv()
            }
            candidates[cell] = mask
        }
        return candidates
    }

    private fun placeAndPropagate(board: IntArray, candidates: IntArray, cell: Int, d: Int) {
        board[cell] = d
        val bit = 1 shl (d - 1)
        for (peer in units.peers[cell]) {
            if (board[peer] == 0) candidates[peer] = candidates[peer] and bit.inv()
        }
    }

    private fun applyNakedSingle(board: IntArray, candidates: IntArray): Technique? {
        var appliedAny = false
        for (cell in 0 until BOARD_SIZE) {
            if (board[cell] != 0) continue
            val mask = candidates[cell]
            if (Integer.bitCount(mask) == 1) {
                placeAndPropagate(board, candidates, cell, Integer.numberOfTrailingZeros(mask) + 1)
                appliedAny = true
            }
        }
        return if (appliedAny) Technique.NAKED_SINGLE else null
    }

    private fun applyHiddenSingle(board: IntArray, candidates: IntArray): Technique? {
        var appliedAny = false
        for (unit in units.units) {
            for (d in 1..9) {
                val bit = 1 shl (d - 1)
                var candidateCell = -1
                var count = 0
                for (cell in unit) {
                    if (board[cell] == 0 && candidates[cell] and bit != 0) {
                        count++
                        candidateCell = cell
                        if (count > 1) break
                    }
                }
                if (count == 1) {
                    placeAndPropagate(board, candidates, candidateCell, d)
                    appliedAny = true
                }
            }
        }
        return if (appliedAny) Technique.HIDDEN_SINGLE else null
    }

    private fun applyLockedCandidates(board: IntArray, candidates: IntArray): Technique? {
        var appliedAny = false
        val allUnits = units.units
        val inB = BooleanArray(BOARD_SIZE)
        for (a in allUnits) {
            for (b in allUnits) {
                if (a === b) continue
                for (cell in b) inB[cell] = true
                val intersection = a.filter { inB[it] }
                if (intersection.isNotEmpty()) {
                    for (d in 1..9) {
                        val bit = 1 shl (d - 1)
                        var anyCandidateInA = false
                        var allInIntersection = true
                        for (cell in a) {
                            if (board[cell] == 0 && candidates[cell] and bit != 0) {
                                anyCandidateInA = true
                                if (cell !in intersection) {
                                    allInIntersection = false
                                    break
                                }
                            }
                        }
                        if (anyCandidateInA && allInIntersection) {
                            for (cell in b) {
                                if (cell !in intersection && board[cell] == 0 && candidates[cell] and bit != 0) {
                                    candidates[cell] = candidates[cell] and bit.inv()
                                    appliedAny = true
                                }
                            }
                        }
                    }
                }
                for (cell in b) inB[cell] = false
            }
        }
        return if (appliedAny) Technique.LOCKED_CANDIDATES else null
    }

    private fun applyNakedPair(board: IntArray, candidates: IntArray): Technique? {
        var appliedAny = false
        for (unit in units.units) {
            val pairCells = unit.filter { board[it] == 0 && Integer.bitCount(candidates[it]) == 2 }
            for (i in pairCells.indices) {
                for (j in i + 1 until pairCells.size) {
                    val c1 = pairCells[i]
                    val c2 = pairCells[j]
                    if (candidates[c1] != candidates[c2]) continue
                    val mask = candidates[c1]
                    for (cell in unit) {
                        if (cell != c1 && cell != c2 && board[cell] == 0 && candidates[cell] and mask != 0) {
                            candidates[cell] = candidates[cell] and mask.inv()
                            appliedAny = true
                        }
                    }
                }
            }
        }
        return if (appliedAny) Technique.NAKED_PAIR else null
    }
}
