package com.zoya.sudoku.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zoya.sudoku.engine.RegionLayout
import com.zoya.sudoku.ui.components.SudokuGridView

/** Small, non-interactive preview of a saved region layout for a library list row. */
@Composable
fun RegionThumbnail(layout: RegionLayout, modifier: Modifier = Modifier) {
    SudokuGridView(cellRegion = { cell -> layout.cellRegion[cell] }, modifier = modifier)
}
