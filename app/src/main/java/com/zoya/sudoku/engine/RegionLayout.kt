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
