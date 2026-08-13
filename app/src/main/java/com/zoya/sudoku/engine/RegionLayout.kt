package com.zoya.sudoku.engine

const val GRID_DIM = 9
const val BOARD_SIZE = GRID_DIM * GRID_DIM

fun rowOf(cell: Int): Int = cell / GRID_DIM
fun colOf(cell: Int): Int = cell % GRID_DIM

/**
 * Assignment of each of the 81 cells to one of 9 regions (0..8). Regions are not required to be
 * geometrically contiguous - the constraint math below only cares that a region is a set of 9 cells.
 */
class RegionLayout(val cellRegion: IntArray) {
    init {
        require(cellRegion.size == BOARD_SIZE) { "cellRegion must have $BOARD_SIZE entries" }
    }

    companion object {
        /** The classic 3x3-box layout, used as a regression baseline for the generic engine. */
        fun classicBoxes(): RegionLayout {
            val cells = IntArray(BOARD_SIZE) { cell -> (rowOf(cell) / 3) * 3 + colOf(cell) / 3 }
            return RegionLayout(cells)
        }
    }
}

/** Precomputes the 27 constraint units (rows, columns, regions) and each cell's peers. */
class Units(layout: RegionLayout) {
    val units: List<IntArray>
    val peers: Array<IntArray>

    init {
        val rowUnits = (0 until GRID_DIM).map { r -> IntArray(GRID_DIM) { c -> r * GRID_DIM + c } }
        val colUnits = (0 until GRID_DIM).map { c -> IntArray(GRID_DIM) { r -> r * GRID_DIM + c } }
        val regionUnits = (0 until GRID_DIM).map { regionId ->
            (0 until BOARD_SIZE).filter { cell -> layout.cellRegion[cell] == regionId }.toIntArray()
        }
        units = rowUnits + colUnits + regionUnits

        val peerSets = Array(BOARD_SIZE) { LinkedHashSet<Int>() }
        for (unit in units) {
            for (cell in unit) {
                for (other in unit) {
                    if (other != cell) peerSets[cell].add(other)
                }
            }
        }
        peers = Array(BOARD_SIZE) { cell -> peerSets[cell].toIntArray() }
    }
}

/**
 * Does any valid sudoku solution exist on this region partition at all? Proving a layout is
 * *invalid* is the expensive search direction (no early exit on first success), so this uses a
 * generous node budget and treats hitting it as "couldn't prove solvable" rather than letting
 * [SolverBudgetExceeded] propagate and crash the caller - this is a one-off action (Constructor's
 * "Save"), not something called repeatedly, so the extra budget is cheap to afford.
 */
fun RegionLayout.isSolvable(): Boolean {
    val units = Units(this)
    return try {
        SudokuSolver(units, nodeLimit = 20_000_000).solve(IntArray(BOARD_SIZE)) != null
    } catch (e: SolverBudgetExceeded) {
        false
    }
}

/**
 * Live paint-time check for the Constructor: would painting [cell] with [color] still leave a
 * valid Sudoku reachable? [cellColors] may be partial (unpainted cells hold any value outside
 * 0..8, e.g. -1), so this only enforces what's already decided - rows, columns, and same-color
 * cells so far must be pairwise distinct - and doesn't require any region to already be full.
 * Building [Units] from a [RegionLayout] over the still-partial array works because an unpainted
 * cell never equals a real region id 0..8, so it simply isn't part of any region unit yet.
 *
 * A "false" here is a hard proof this exact paint is a dead end. A "true" is not a promise the
 * *finished* layout will solve - later paints can still box the puzzle in - so [isSolvable] on
 * Save remains the authoritative check; this is only meant to catch the obvious mistakes early.
 */
fun wouldRemainFeasible(cellColors: List<Int>, cell: Int, color: Int, nodeLimit: Int = 50_000): Boolean {
    val trial = IntArray(BOARD_SIZE) { i -> if (i == cell) color else cellColors[i] }
    val units = Units(RegionLayout(trial))
    return try {
        SudokuSolver(units, nodeLimit = nodeLimit).solve(IntArray(BOARD_SIZE)) != null
    } catch (e: SolverBudgetExceeded) {
        true
    }
}

/**
 * On-demand replacement for the Constructor's "the cached witness conflicts here" path: given the
 * current partial [cellColors] with [cell] now set to [color], searches for ANY full valid Sudoku
 * grid consistent with what's decided so far (same partial-[Units] trick as [wouldRemainFeasible]
 * - unpainted cells aren't part of any region unit yet). Unlike [wouldRemainFeasible]'s "prove no
 * solution exists" direction, this is "find a solution", which exits on first success and is fast
 * for realistic (non-adversarial) partial layouts - so hitting the node budget here is a real
 * failure signal, not something to optimistically wave through.
 */
fun regenerateWitness(cellColors: List<Int>, cell: Int, color: Int, nodeLimit: Int = 500_000): IntArray? {
    val trial = IntArray(BOARD_SIZE) { i -> if (i == cell) color else cellColors[i] }
    val units = Units(RegionLayout(trial))
    return try {
        SudokuSolver(units, nodeLimit = nodeLimit).solve(IntArray(BOARD_SIZE), randomize = true)
    } catch (e: SolverBudgetExceeded) {
        null
    }
}
