package com.zoya.sudoku.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "region_layouts")
data class RegionLayoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** 81 chars, one per cell, region id '0'-'8'. */
    val cellRegions: String,
    val createdAt: Long
)

@Dao
interface RegionLayoutDao {
    @Insert
    suspend fun insert(layout: RegionLayoutEntity): Long

    @Query("SELECT * FROM region_layouts ORDER BY createdAt DESC")
    fun getAll(): Flow<List<RegionLayoutEntity>>

    @Query("SELECT * FROM region_layouts WHERE id = :id")
    suspend fun getById(id: Long): RegionLayoutEntity?

    @Query("SELECT COUNT(*) FROM region_layouts")
    suspend fun count(): Int

    @Query("DELETE FROM region_layouts WHERE id = :id")
    suspend fun delete(id: Long)
}
