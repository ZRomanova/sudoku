package com.zoya.sudoku.data.repository

import com.zoya.sudoku.data.decodeDigits
import com.zoya.sudoku.data.encodeDigits
import com.zoya.sudoku.data.db.PuzzleStateDao
import com.zoya.sudoku.data.db.PuzzleStateEntity
import com.zoya.sudoku.data.db.RegionLayoutDao
import com.zoya.sudoku.engine.Difficulty
import com.zoya.sudoku.engine.PuzzleGenerator
import com.zoya.sudoku.engine.RegionLayout
import com.zoya.sudoku.engine.Units
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow

class PuzzleRepository(
    private val puzzleDao: PuzzleStateDao,
    private val layoutDao: RegionLayoutDao
) {
    fun observeCurrent(): Flow<PuzzleStateEntity?> = puzzleDao.getCurrent()

    suspend fun hasActivePuzzle(): Boolean = puzzleDao.getCurrentOnce() != null

    /**
     * Generates a brand-new solution + given-set for [layoutId] at [difficulty] and makes it the
     * active puzzle. Difficulty is chosen at Play time, not baked into the saved layout - the same
     * region shape can be replayed at any difficulty.
     */
    suspend fun generateAndStartNew(layoutId: Long, difficulty: Difficulty, rng: Random = Random(System.nanoTime())) {
        val layoutEntity = requireNotNull(layoutDao.getById(layoutId)) { "Unknown layout $layoutId" }
        val layout = RegionLayout(layoutEntity.cellRegions.decodeDigits())
        val generator = PuzzleGenerator(Units(layout))
        val puzzle = generator.generatePuzzle(difficulty, rng)

        val givens = puzzle.givens.encodeDigits()
        puzzleDao.upsert(
            PuzzleStateEntity(
                layoutId = layoutId,
                solution = puzzle.solution.encodeDigits(),
                givens = givens,
                board = givens,
                difficulty = puzzle.difficulty
            )
        )
    }

    /** Ends the current puzzle for good (no history is kept, matching the app's no-clutter design). */
    suspend fun finishCurrent() {
        puzzleDao.clear()
    }

    /** Autosaves a digit entry; a no-op if [cell] is a given. */
    suspend fun applyMove(cell: Int, digit: Int) {
        mutateBoard(cell) { board -> board[cell] = '0' + digit }
    }

    /** Autosaves an erase; a no-op if [cell] is a given. */
    suspend fun eraseMove(cell: Int) {
        mutateBoard(cell) { board -> board[cell] = '0' }
    }

    private suspend fun mutateBoard(cell: Int, edit: (CharArray) -> Unit) {
        val current = puzzleDao.getCurrentOnce() ?: return
        if (current.givens[cell] != '0') return
        val board = current.board.toCharArray()
        edit(board)
        puzzleDao.upsert(current.copy(board = String(board)))
    }
}
