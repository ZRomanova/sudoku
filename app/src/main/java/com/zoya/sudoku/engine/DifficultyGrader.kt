package com.zoya.sudoku.engine

/** Ladder of solving techniques, cheapest/most-obvious first. Ordinal order == difficulty order. */
enum class Technique { NAKED_SINGLE, HIDDEN_SINGLE, LOCKED_CANDIDATES, NAKED_PAIR }

data class Grade(
    val maxTechnique: Technique?,
    val requiresGuessing: Boolean,
    val solvedCompletely: Boolean,
    val givenCount: Int
)

/** Result of a logic-only solve: the [grade] plus the board as far as logic got (full if solved). */
class LogicSolveResult(val grade: Grade, val board: IntArray)

/**
 * Simulates a human solving the puzzle using only [Technique]s, always applying the cheapest
 * applicable one first. Whatever it can't resolve this way is treated as "requires guessing".
 *
 * Every technique here is a sound deduction (it only ever removes candidates that can't be in ANY
 * solution), so when this solves a puzzle completely, that completion is the puzzle's only
 * solution - [PuzzleCarver] relies on this to guarantee uniqueness AND human-solvability at once.
 *
 * Classic "pointing pairs" / "box-line reduction" assume a box spans exactly 3 rows x 3 columns,
 * which doesn't hold for arbitrary regions. [LOCKED_CANDIDATES] here is the shape-agnostic
 * generalization: for any two units A, B, if all of digit d's remaining candidates in A lie
 * within A intersect B, d can be eliminated from B outside that intersection.
 */
class DifficultyGrader(private val units: Units) {

    /**
     * Unit pairs whose overlap has 2+ cells - the only ones [applyLockedCandidates] can learn
     * anything from (a 1-cell overlap is just a hidden single, already tried first). Precomputed
     * once because the carver grades the same layout dozens of times per puzzle.
     */
    private val lockedPairs: List<LockedPair> = buildList {
        for (a in units.units) {
            for (b in units.units) {
                if (a === b) continue
                val inA = BooleanArray(BOARD_SIZE).also { m -> a.forEach { m[it] = true } }
                val inB = BooleanArray(BOARD_SIZE).also { m -> b.forEach { m[it] = true } }
                val overlap = a.count { inB[it] }
                if (overlap in 2 until GRID_DIM) add(LockedPair(a, b, inA, inB))
            }
        }
    }

    private class LockedPair(val a: IntArray, val b: IntArray, val inA: BooleanArray, val inB: BooleanArray)

    fun gradePuzzle(givens: IntArray, maxAllowed: Technique = Technique.entries.last()): Grade =
        solveLogically(givens, maxAllowed).grade

    /** Like [gradePuzzle], but only ever uses techniques up to [maxAllowed] and returns the board too. */
    fun solveLogically(givens: IntArray, maxAllowed: Technique = Technique.entries.last()): LogicSolveResult {
        val board = givens.copyOf()
        val candidates = buildCandidates(board)
        var maxTechnique: Technique? = null
        val givenCount = givens.count { it != 0 }

        while (true) {
            if (board.all { it != 0 }) {
                return LogicSolveResult(
                    Grade(maxTechnique, requiresGuessing = false, solvedCompletely = true, givenCount = givenCount),
                    board
                )
            }
            if (hasDeadCell(board, candidates)) break
            val applied = applyNakedSingle(board, candidates)
                ?: applyHiddenSingle(board, candidates)
                ?: (if (maxAllowed >= Technique.LOCKED_CANDIDATES) applyLockedCandidates(board, candidates) else null)
                ?: (if (maxAllowed >= Technique.NAKED_PAIR) applyNakedPair(board, candidates) else null)
                ?: break

            if (maxTechnique == null || applied.ordinal > maxTechnique.ordinal) {
                maxTechnique = applied
            }
        }
        return LogicSolveResult(
            Grade(maxTechnique, requiresGuessing = true, solvedCompletely = false, givenCount = givenCount),
            board
        )
    }

    /** An empty cell with no candidates left means the givens contradict each other - stop there. */
    private fun hasDeadCell(board: IntArray, candidates: IntArray): Boolean {
        for (cell in 0 until BOARD_SIZE) {
            if (board[cell] == 0 && candidates[cell] == 0) return true
        }
        return false
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
        for (pair in lockedPairs) {
            for (d in 1..9) {
                val bit = 1 shl (d - 1)
                var anyCandidateInA = false
                var allInIntersection = true
                for (cell in pair.a) {
                    if (board[cell] == 0 && candidates[cell] and bit != 0) {
                        anyCandidateInA = true
                        if (!pair.inB[cell]) {
                            allInIntersection = false
                            break
                        }
                    }
                }
                if (!anyCandidateInA || !allInIntersection) continue
                for (cell in pair.b) {
                    if (!pair.inA[cell] && board[cell] == 0 && candidates[cell] and bit != 0) {
                        candidates[cell] = candidates[cell] and bit.inv()
                        appliedAny = true
                    }
                }
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
