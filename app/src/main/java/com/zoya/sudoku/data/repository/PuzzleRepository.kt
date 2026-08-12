package com.zoya.sudoku.data.repository

import com.zoya.sudoku.data.decodeDigits
import com.zoya.sudoku.data.decodeNoteMasks
import com.zoya.sudoku.data.emptyNoteMasks
import com.zoya.sudoku.data.encodeDigits
import com.zoya.sudoku.data.encodeNoteMasks
import com.zoya.sudoku.data.db.PuzzleStateDao
import com.zoya.sudoku.data.db.PuzzleStateEntity
import com.zoya.sudoku.data.db.RegionLayoutDao
import com.zoya.sudoku.engine.Difficulty
import com.zoya.sudoku.engine.PuzzleGenerator
import com.zoya.sudoku.engine.RegionLayout
import com.zoya.sudoku.engine.Units
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** An in-progress puzzle enriched with its layout's name/shape, for the Home "Продолжить" list. */
data class InProgressPuzzle(
    val id: Long,
    val layoutId: Long,
    val layoutName: String,
    val layout: RegionLayout,
    val difficulty: Difficulty,
    val updatedAt: Long
)

class PuzzleRepository(
    private val puzzleDao: PuzzleStateDao,
    private val layoutDao: RegionLayoutDao
) {
    fun observe(id: Long): Flow<PuzzleStateEntity?> = puzzleDao.observe(id)

    /** All puzzles currently in progress, most recently touched first. Several can share a
     *  layoutId - each started game keeps its own row, never overwriting another's progress. */
    fun observeInProgress(): Flow<List<InProgressPuzzle>> =
        combine(puzzleDao.observeAll(), layoutDao.getAll()) { puzzles, layouts ->
            val layoutsById = layouts.associateBy { it.id }
            puzzles.mapNotNull { puzzle ->
                val layoutEntity = layoutsById[puzzle.layoutId] ?: return@mapNotNull null
                InProgressPuzzle(
                    id = puzzle.id,
                    layoutId = puzzle.layoutId,
                    layoutName = layoutEntity.name,
                    layout = RegionLayout(layoutEntity.cellRegions.decodeDigits()),
                    difficulty = puzzle.difficulty,
                    updatedAt = puzzle.updatedAt
                )
            }
        }

    /**
     * Generates a brand-new solution + given-set for [layoutId] at [difficulty] and starts it as
     * a new resumable puzzle, returning its id. Difficulty is chosen at Play time, not baked into
     * the saved layout - the same region shape can be replayed at any difficulty, any number of
     * times, with every run kept independently (no overwriting an existing run on the same layout).
     */
    suspend fun generateAndStartNew(layoutId: Long, difficulty: Difficulty, rng: Random = Random(System.nanoTime())): Long {
        val layoutEntity = requireNotNull(layoutDao.getById(layoutId)) { "Unknown layout $layoutId" }
        val layout = RegionLayout(layoutEntity.cellRegions.decodeDigits())
        val generator = PuzzleGenerator(Units(layout))
        val puzzle = generator.generatePuzzle(difficulty, rng)

        val givens = puzzle.givens.encodeDigits()
        return puzzleDao.insert(
            PuzzleStateEntity(
                layoutId = layoutId,
                solution = puzzle.solution.encodeDigits(),
                givens = givens,
                board = givens,
                notes = emptyNoteMasks().encodeNoteMasks(),
                difficulty = puzzle.difficulty,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /** Ends a puzzle for good, whether finished or abandoned (no history is kept). */
    suspend fun finishCurrent(id: Long) {
        puzzleDao.delete(id)
    }

    /** Autosaves a digit entry; a no-op if [cell] is a given. Clears pencil marks per [clearMode]. */
    suspend fun applyMove(id: Long, cell: Int, digit: Int, clearMode: NoteClearMode) {
        val current = puzzleDao.getOnce(id) ?: return
        if (current.givens[cell] != '0') return

        val board = current.board.toCharArray()
        board[cell] = '0' + digit
        val notes = current.notes.decodeNoteMasks()

        when (clearMode) {
            NoteClearMode.CLEAR_CELL -> notes[cell] = 0
            NoteClearMode.CLEAR_PEERS -> {
                notes[cell] = 0
                val layoutEntity = layoutDao.getById(current.layoutId)
                if (layoutEntity != null) {
                    val layout = RegionLayout(layoutEntity.cellRegions.decodeDigits())
                    val bit = 1 shl (digit - 1)
                    for (peer in Units(layout).peers[cell]) {
                        notes[peer] = notes[peer] and bit.inv()
                    }
                }
            }
            NoteClearMode.KEEP -> {}
        }

        puzzleDao.update(current.copy(board = String(board), notes = notes.encodeNoteMasks(), updatedAt = System.currentTimeMillis()))
    }

    /** Autosaves an erase; a no-op if [cell] is a given. Any preserved notes in the cell resurface. */
    suspend fun eraseMove(id: Long, cell: Int) {
        mutateBoard(id, cell) { board -> board[cell] = '0' }
    }

    /** Toggles [digit] as a candidate in [cell]; a no-op on givens or cells that already hold a value. */
    suspend fun toggleNote(id: Long, cell: Int, digit: Int) {
        val current = puzzleDao.getOnce(id) ?: return
        if (current.givens[cell] != '0' || current.board[cell] != '0') return
        val notes = current.notes.decodeNoteMasks()
        val bit = 1 shl (digit - 1)
        notes[cell] = notes[cell] xor bit
        puzzleDao.update(current.copy(notes = notes.encodeNoteMasks(), updatedAt = System.currentTimeMillis()))
    }

    /** Clears every pencil mark in [cell]. */
    suspend fun clearNotes(id: Long, cell: Int) {
        val current = puzzleDao.getOnce(id) ?: return
        val notes = current.notes.decodeNoteMasks()
        if (notes[cell] == 0) return
        notes[cell] = 0
        puzzleDao.update(current.copy(notes = notes.encodeNoteMasks(), updatedAt = System.currentTimeMillis()))
    }

    /** Wipes every entered digit and pencil mark, putting the board back to its given clues. */
    suspend fun resetProgress(id: Long) {
        val current = puzzleDao.getOnce(id) ?: return
        puzzleDao.update(
            current.copy(
                board = current.givens,
                notes = emptyNoteMasks().encodeNoteMasks(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private suspend fun mutateBoard(id: Long, cell: Int, edit: (CharArray) -> Unit) {
        val current = puzzleDao.getOnce(id) ?: return
        if (current.givens[cell] != '0') return
        val board = current.board.toCharArray()
        edit(board)
        puzzleDao.update(current.copy(board = String(board), updatedAt = System.currentTimeMillis()))
    }
}
