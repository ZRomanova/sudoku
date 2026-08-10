package com.zoya.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.ui.theme.RegionColors

const val ERASER = -1

private val SwatchSize = 46.dp

/**
 * Palette of 9 region colors + eraser laid out as 2 short rows (5 + 4-plus-eraser) instead of a
 * tall 3x3 grid, so the whole Constructor screen fits on a phone without scrolling. A color is
 * disabled - regardless of whether it's still the active tool - the instant it reaches 9/9.
 */
@Composable
fun ColorPalette(
    counts: IntArray,
    activeTool: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (colorIndex in 0..4) {
                Swatch(
                    color = RegionColors[colorIndex],
                    count = counts[colorIndex],
                    selected = activeTool == colorIndex,
                    enabled = counts[colorIndex] < 9,
                    onClick = { onSelect(colorIndex) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (colorIndex in 5..8) {
                Swatch(
                    color = RegionColors[colorIndex],
                    count = counts[colorIndex],
                    selected = activeTool == colorIndex,
                    enabled = counts[colorIndex] < 9,
                    onClick = { onSelect(colorIndex) }
                )
            }
            EraserSwatch(selected = activeTool == ERASER, onClick = { onSelect(ERASER) })
        }
    }
}

@Composable
private fun Swatch(
    color: Color,
    count: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.35f)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(SwatchSize)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                    else Modifier
                )
        )
        Text("$count/9", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EraserSwatch(selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(SwatchSize)
                .clip(CircleShape)
                .border(
                    if (selected) 3.dp else 1.dp,
                    MaterialTheme.colorScheme.onBackground,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("×")
        }
        Text("Ластик", style = MaterialTheme.typography.labelSmall)
    }
}
