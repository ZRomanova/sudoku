package com.zoya.sudoku.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val updatedAtFormat = SimpleDateFormat("d MMM, HH:mm", Locale("ru"))

/** "12 авг, 17:06" - lets identical раскладка+сложность entries in "Продолжить" be told apart. */
fun formatUpdatedAt(millis: Long): String = updatedAtFormat.format(Date(millis))
