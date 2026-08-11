package com.zoya.sudoku.data.repository

import com.zoya.sudoku.data.db.GameResultDao
import com.zoya.sudoku.data.db.GameResultEntity
import com.zoya.sudoku.engine.Difficulty
import com.zoya.sudoku.engine.RegionLayout
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** correct/total for one (layout, difficulty) slice, or for a whole layout across difficulties. */
data class ResultTally(val correct: Int, val total: Int) {
    /** Nearest-integer success rate; 0 when nothing has been finished yet (never divides by zero). */
    val percent: Int get() = if (total == 0) 0 else (correct * 100.0 / total).roundToInt()
}

data class LayoutStats(
    val layoutId: Long,
    val layoutName: String,
    val layout: RegionLayout,
    val overall: ResultTally,
    val byDifficulty: Map<Difficulty, ResultTally>
)

class StatsRepository(
    private val gameResultDao: GameResultDao,
    private val regionLayoutRepository: RegionLayoutRepository
) {
    suspend fun recordResult(layoutId: Long, difficulty: Difficulty, correct: Boolean) {
        gameResultDao.insert(
            GameResultEntity(
                layoutId = layoutId,
                difficulty = difficulty,
                correct = correct,
                completedAt = System.currentTimeMillis()
            )
        )
    }

    /** Only layouts with at least one *finished* game - an unplayed layout has no rate to show. */
    fun observeLayoutStats(): Flow<List<LayoutStats>> =
        combine(gameResultDao.observeAll(), regionLayoutRepository.getAll()) { results, layouts ->
            val byLayoutId = results.groupBy { it.layoutId }
            layouts.mapNotNull { saved ->
                val layoutResults = byLayoutId[saved.id] ?: return@mapNotNull null
                val byDifficulty = Difficulty.entries.associateWith { difficulty ->
                    layoutResults.filter { it.difficulty == difficulty }.toTally()
                }
                LayoutStats(
                    layoutId = saved.id,
                    layoutName = saved.name,
                    layout = saved.layout,
                    overall = layoutResults.toTally(),
                    byDifficulty = byDifficulty
                )
            }
        }

    suspend fun reset() = gameResultDao.clear()

    private fun List<GameResultEntity>.toTally() = ResultTally(correct = count { it.correct }, total = size)
}
