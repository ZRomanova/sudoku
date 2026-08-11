package com.zoya.sudoku.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.zoya.sudoku.engine.Difficulty
import kotlinx.coroutines.flow.Flow

/**
 * One row per *finished* game (the player tapped "Завершить" on a full board) - a game abandoned
 * mid-play (Home button, app close) never reaches [GameResultDao.insert], so it never counts
 * toward statistics. [correct] is whether the board matched the solution at the moment of finishing.
 */
@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val layoutId: Long,
    val difficulty: Difficulty,
    val correct: Boolean,
    val completedAt: Long
)

@Dao
interface GameResultDao {
    @Insert
    suspend fun insert(result: GameResultEntity)

    @Query("SELECT * FROM game_results")
    fun observeAll(): Flow<List<GameResultEntity>>

    @Query("DELETE FROM game_results")
    suspend fun clear()
}
