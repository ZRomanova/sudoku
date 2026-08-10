package com.zoya.sudoku.data

/** Encodes an 81-cell digit array (0 = blank) as a fixed-width string, one char per cell. */
fun IntArray.encodeDigits(): String = joinToString("") { it.toString() }

fun String.decodeDigits(): IntArray = IntArray(length) { i -> this[i] - '0' }
