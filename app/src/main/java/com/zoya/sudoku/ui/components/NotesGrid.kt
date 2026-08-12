package com.zoya.sudoku.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A 3x3 grid of pencilled-in candidate digits (1-9) filling a cell, per [mask] bit (digit-1). */
@Composable
fun NotesGrid(mask: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(2.dp)) {
        for (row in 0 until 3) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                for (col in 0 until 3) {
                    val digit = row * 3 + col + 1
                    Text(
                        text = if (mask and (1 shl (digit - 1)) != 0) digit.toString() else "",
                        color = color,
                        fontSize = 9.sp,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }
            }
        }
    }
}
