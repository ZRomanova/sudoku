package com.zoya.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.engine.Difficulty
import com.zoya.sudoku.ui.displayName
import com.zoya.sudoku.ui.theme.GridLineColor

/** Shared difficulty picker used everywhere a puzzle is about to be generated (Library, Home). */
@Composable
fun DifficultyDialog(onDismiss: () -> Unit, onStart: (Difficulty) -> Unit) {
    var selected by remember { mutableStateOf(Difficulty.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сложность") },
        text = {
            Column {
                for (difficulty in Difficulty.entries) {
                    DifficultyOption(
                        label = difficulty.displayName(),
                        selected = selected == difficulty,
                        onClick = { selected = difficulty }
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onStart(selected) }) { Text("Старт") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

/**
 * Hand-rolled in-house FilterChip replacement: selected/unselected only differ by fill color,
 * both keep the same border, shape and text style, so they read as one family instead of two
 * different components. Selected fill is the theme's primary color - the same purple every other
 * button (Старт, Продолжить, Сохранить...) already uses, so this fits the rest of the app instead
 * of introducing its own accent.
 */
@Composable
private fun DifficultyOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(shape)
            .background(if (selected) primary else Color.Transparent)
            .border(1.dp, if (selected) primary else GridLineColor.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
        )
    }
}
