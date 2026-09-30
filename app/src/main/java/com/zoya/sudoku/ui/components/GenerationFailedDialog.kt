package com.zoya.sudoku.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Shown instead of crashing if a new puzzle couldn't be generated for the chosen раскладка. */
@Composable
fun GenerationFailedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Понятно") } },
        title = { Text("Не удалось создать игру") },
        text = { Text("Для этой раскладки не получилось построить судоку. Попробуйте ещё раз или выберите другую раскладку.") }
    )
}
