package com.zoya.sudoku.data.repository

import com.zoya.sudoku.data.decodeDigits
import com.zoya.sudoku.data.encodeDigits
import com.zoya.sudoku.data.db.RegionLayoutDao
import com.zoya.sudoku.data.db.RegionLayoutEntity
import com.zoya.sudoku.engine.RegionLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SavedLayout(
    val id: Long,
    val name: String,
    val layout: RegionLayout,
    val createdAt: Long
)

class RegionLayoutRepository(private val dao: RegionLayoutDao) {

    fun getAll(): Flow<List<SavedLayout>> = dao.getAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun save(layout: RegionLayout, name: String): Long =
        dao.insert(
            RegionLayoutEntity(
                name = name,
                cellRegions = layout.cellRegion.encodeDigits(),
                createdAt = System.currentTimeMillis()
            )
        )

    suspend fun getById(id: Long): SavedLayout? = dao.getById(id)?.toDomain()

    suspend fun rename(id: Long, name: String) = dao.rename(id, name)

    suspend fun delete(id: Long) = dao.delete(id)

    /** Used to suggest the next "Раскладка #N" default name in the Constructor. */
    suspend fun count(): Int = dao.count()

    /** On first-ever run (nothing saved yet) seeds one ready-to-play classic-box layout. */
    suspend fun ensureDefaultLayoutExists() {
        if (dao.count() == 0) {
            save(RegionLayout.classicBoxes(), "Классическое судоку")
        }
    }

    /**
     * Picks a random saved layout for "Случайная игра". Re-seeds the classic layout first if the
     * user deleted every saved layout, so this always has something to return.
     */
    suspend fun randomLayoutId(): Long {
        ensureDefaultLayoutExists()
        return dao.getAllIds().random()
    }

    private fun RegionLayoutEntity.toDomain() = SavedLayout(
        id = id,
        name = name,
        layout = RegionLayout(cellRegions.decodeDigits()),
        createdAt = createdAt
    )
}
