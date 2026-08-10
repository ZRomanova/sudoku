package com.zoya.sudoku.data.db

import androidx.room.TypeConverter
import com.zoya.sudoku.engine.Difficulty

class Converters {
    @TypeConverter
    fun fromDifficulty(value: Difficulty): String = value.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty = Difficulty.valueOf(value)
}
