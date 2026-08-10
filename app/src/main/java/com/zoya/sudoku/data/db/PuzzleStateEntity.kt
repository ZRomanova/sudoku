package com.zoya.sudoku.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.zoya.sudoku.engine.Difficulty
import kotlinx.coroutines.flow.Flow

/**
 * The single resumable puzzle - "opening the app always drops you back where you left off" means
 * there is only ever one in-progress game, not a history of them. A fresh "Play" tap overwrites it.
 */
@Entity(tableName = "puzzle_state")
data class PuzzleStateEntity(
    @PrimaryKey val id: Long = 1,
    val layoutId: Long,
    /** 81 chars, the full solution (kept for a possible future subtle completion cue). */
    val solution: String,
    /** 81 chars, '0' = blank; non-zero cells are fixed givens the player can't edit. */
    val givens: String,
    /** 81 chars, live board state = givens overlaid with the player's entries. */
    val board: String,
    val difficulty: Difficulty
)

@Dao
interface PuzzleStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: PuzzleStateEntity)

    @Query("SELECT * FROM puzzle_state WHERE id = 1")
    fun getCurrent(): Flow<PuzzleStateEntity?>

    @Query("SELECT * FROM puzzle_state WHERE id = 1")
    suspend fun getCurrentOnce(): PuzzleStateEntity?

    @Query("DELETE FROM puzzle_state WHERE id = 1")
    suspend fun clear()
}
