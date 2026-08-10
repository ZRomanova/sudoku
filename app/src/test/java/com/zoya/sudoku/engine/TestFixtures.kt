package com.zoya.sudoku.engine

/**
 * cellRegion[cell] = (row+col) mod 9 - each region is a "broken diagonal": exactly one cell per
 * row and one per column (so the 9-per-region precondition holds), but no resemblance to a 3x3
 * box - a generic non-square fixture for exercising the engine.
 *
 * Provably solvable: digit(r,c) = (r + 2c) mod 9 + 1 is a valid full solution for it. For a fixed
 * region v (cells where r+c=v mod 9), c = (v-r) mod 9, so digit(r,c) = (r + 2*((v-r) mod 9)) mod 9
 * + 1, which simplifies to (2v - r) mod 9 + 1 - a bijection of r over Z/9, so every digit 1..9
 * appears exactly once in the region. It's also a standard Latin square (coprime slope 2 mod 9),
 * so rows/columns hold too.
 */
fun diagonalJigsaw(): RegionLayout =
    RegionLayout(IntArray(BOARD_SIZE) { cell -> (rowOf(cell) + colOf(cell)) % GRID_DIM })

val diagonalJigsawSolution: IntArray =
    IntArray(BOARD_SIZE) { cell -> (rowOf(cell) + 2 * colOf(cell)) % GRID_DIM + 1 }

fun isValidCompleteSolution(board: IntArray, units: Units): Boolean {
    if (board.any { it !in 1..9 }) return false
    return units.units.all { unit -> unit.map { board[it] }.toSet().size == GRID_DIM }
}
