package com.zoya.sudoku.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.zoya.sudoku.engine.Difficulty
import kotlinx.coroutines.flow.Flow

/**
 * A resumable puzzle. Several can exist for the same [layoutId] at once (e.g. two separate
 * "Случайная игра" runs that happened to land on the same раскладка) - each keeps its own
 * progress, distinguished by [updatedAt] in the "Продолжить" list. Nothing is ever silently
 * overwritten by starting a new game.
 */
@Entity(tableName = "puzzle_state")
data class PuzzleStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val layoutId: Long,
    /** 81 chars, the full solution (kept for a possible future subtle completion cue). */
    val solution: String,
    /** 81 chars, '0' = blank; non-zero cells are fixed givens the player can't edit. */
    val givens: String,
    /** 81 chars, live board state = givens overlaid with the player's entries. */
    val board: String,
    /** 81 comma-joined bitmasks, one per cell - bit (digit-1) set means that digit is pencilled in. */
    val notes: String,
    val difficulty: Difficulty,
    val updatedAt: Long
)

@Dao
interface PuzzleStateDao {
    @Insert
    suspend fun insert(state: PuzzleStateEntity): Long

    @Update
    suspend fun update(state: PuzzleStateEntity)

    @Query("SELECT * FROM puzzle_state WHERE id = :id")
    fun observe(id: Long): Flow<PuzzleStateEntity?>

    @Query("SELECT * FROM puzzle_state WHERE id = :id")
    suspend fun getOnce(id: Long): PuzzleStateEntity?

    @Query("SELECT * FROM puzzle_state ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<PuzzleStateEntity>>

    @Query("DELETE FROM puzzle_state WHERE id = :id")
    suspend fun delete(id: Long)
}
