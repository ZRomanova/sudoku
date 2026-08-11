package com.zoya.sudoku.ui.theme

import androidx.compose.ui.graphics.Color

/** 9 mutually-distinguishable, muted region colors (colorblind support intentionally out of scope). */
val RegionColors: List<Color> = listOf(
    Color(0xFFE07A5F), // terracotta
    Color(0xFF3D5A80), // indigo blue
    Color(0xFF2A9D8F), // teal green
    Color(0xFFF2CC8F), // golden sand
    Color(0xFF6D597A), // plum purple
    Color(0xFFC97B84), // dusty rose
    Color(0xFF588157), // forest green
    Color(0xFF9C6644), // ochre brown
    Color(0xFF6C757D)  // slate gray
)

val AppBackground = Color(0xFFF4F1EC)
val GridLineColor = Color(0xFF2B2B2B)
val CellSelectedOverlay = Color(0x33000000)

/** Tint for a cell the Constructor has proven can't take the active color without dooming the layout. */
val BlockedCellOverlay = Color(0x4DD1495B)

/** Used only for the on-demand, post-completion error check - not part of normal play styling. */
val ErrorDigitColor = Color(0xFFD1495B)

/** Black or white digit ink, whichever reads better on this region background. */
fun contrastingDigitColor(background: Color): Color {
    val luminance = 0.299 * background.red + 0.587 * background.green + 0.114 * background.blue
    return if (luminance > 0.55) Color.Black else Color.White
}
