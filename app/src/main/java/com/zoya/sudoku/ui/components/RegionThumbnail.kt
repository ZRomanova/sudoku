package com.zoya.sudoku.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zoya.sudoku.engine.RegionLayout

/** Small, non-interactive preview of a saved region layout - used in Library and Stats rows. */
@Composable
fun RegionThumbnail(layout: RegionLayout, modifier: Modifier = Modifier) {
    SudokuGridView(cellRegion = { cell -> layout.cellRegion[cell] }, modifier = modifier)
}
