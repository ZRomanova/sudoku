package com.zoya.sudoku.ui

import com.zoya.sudoku.engine.Difficulty

fun Difficulty.displayName(): String = when (this) {
    Difficulty.EASY -> "Лёгкий"
    Difficulty.MEDIUM -> "Средний"
    Difficulty.HARD -> "Сложный"
}
