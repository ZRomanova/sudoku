package com.zoya.sudoku.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.engine.BOARD_SIZE
import com.zoya.sudoku.engine.GRID_DIM
import com.zoya.sudoku.engine.colOf
import com.zoya.sudoku.engine.rowOf
import com.zoya.sudoku.ui.theme.CellSelectedOverlay
import com.zoya.sudoku.ui.theme.GridLineColor
import com.zoya.sudoku.ui.theme.RegionColors

/**
 * Renders the 9x9 board: cell backgrounds from region colors, thick lines only where two
 * neighboring cells actually belong to different regions (or the board edge), thin lines
 * elsewhere - so the grid always reflects the real region shape, never an assumed 3x3 layout.
 * [cellRegion] returns -1 for a still-uncolored cell (constructor mode only).
 *
 * [dragPaint] switches the tap handling from one-cell-per-tap (Game's select-then-type flow) to a
 * single continuous gesture over the whole board, so dragging a finger across cells calls
 * [onCellTap] for every cell it crosses (Constructor's paint-with-a-stroke flow).
 */
@Composable
fun SudokuGridView(
    cellRegion: (Int) -> Int,
    modifier: Modifier = Modifier,
    selectedCell: Int? = null,
    onCellTap: ((Int) -> Unit)? = null,
    dragPaint: Boolean = false,
    cellContent: (@Composable BoxScope.(cell: Int) -> Unit)? = null
) {
    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val cellSizeDp = maxWidth / GRID_DIM

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .let { base ->
                    if (onCellTap != null && dragPaint) {
                        // Keyed on Unit (not onCellTap) so a mid-drag recomposition - which
                        // every painted cell triggers - never restarts and drops the gesture.
                        base.pointerInput(Unit) {
                            val cellPx = size.width / GRID_DIM.toFloat()
                            fun cellAt(offset: Offset): Int {
                                val col = (offset.x / cellPx).toInt().coerceIn(0, GRID_DIM - 1)
                                val row = (offset.y / cellPx).toInt().coerceIn(0, GRID_DIM - 1)
                                return row * GRID_DIM + col
                            }
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                onCellTap(cellAt(down.position))
                                down.consume()
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break
                                    if (!change.pressed) break
                                    onCellTap(cellAt(change.position))
                                    change.consume()
                                }
                            }
                        }
                    } else {
                        base
                    }
                }
        ) {
            val cellPx = size.width / GRID_DIM
            val thin = 1.dp.toPx()
            val thick = 3.dp.toPx()

            for (cell in 0 until BOARD_SIZE) {
                val r = rowOf(cell)
                val c = colOf(cell)
                val region = cellRegion(cell)
                val color = if (region in RegionColors.indices) RegionColors[region] else Color.White
                drawRect(
                    color = color,
                    topLeft = Offset(c * cellPx, r * cellPx),
                    size = Size(cellPx, cellPx)
                )
                if (selectedCell == cell) {
                    drawRect(
                        color = CellSelectedOverlay,
                        topLeft = Offset(c * cellPx, r * cellPx),
                        size = Size(cellPx, cellPx)
                    )
                }
            }

            for (cell in 0 until BOARD_SIZE) {
                val r = rowOf(cell)
                val c = colOf(cell)
                val region = cellRegion(cell)
                if (c < GRID_DIM - 1) {
                    val strokeWidth = if (cellRegion(cell + 1) != region) thick else thin
                    drawLine(
                        GridLineColor,
                        Offset((c + 1) * cellPx, r * cellPx),
                        Offset((c + 1) * cellPx, (r + 1) * cellPx),
                        strokeWidth = strokeWidth
                    )
                }
                if (r < GRID_DIM - 1) {
                    val strokeWidth = if (cellRegion(cell + GRID_DIM) != region) thick else thin
                    drawLine(
                        GridLineColor,
                        Offset(c * cellPx, (r + 1) * cellPx),
                        Offset((c + 1) * cellPx, (r + 1) * cellPx),
                        strokeWidth = strokeWidth
                    )
                }
            }

            drawRect(color = GridLineColor, size = size, style = Stroke(width = thick))
        }

        val tapPerCell = onCellTap != null && !dragPaint
        if (tapPerCell || cellContent != null) {
            Column {
                for (r in 0 until GRID_DIM) {
                    Row {
                        for (c in 0 until GRID_DIM) {
                            val cell = r * GRID_DIM + c
                            Box(
                                modifier = Modifier
                                    .size(cellSizeDp)
                                    .let { m -> if (tapPerCell) m.clickable { onCellTap?.invoke(cell) } else m },
                                contentAlignment = Alignment.Center
                            ) {
                                cellContent?.invoke(this, cell)
                            }
                        }
                    }
                }
            }
        }
    }
}
