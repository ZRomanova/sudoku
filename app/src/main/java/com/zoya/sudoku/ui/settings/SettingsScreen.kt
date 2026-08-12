package com.zoya.sudoku.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.data.repository.NoteClearMode
import com.zoya.sudoku.ui.components.ScreenHeader
import com.zoya.sudoku.ui.theme.GridLineColor

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onHome: () -> Unit) {
    val noteClearMode by viewModel.noteClearMode.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ScreenHeader("Настройки", onHome)
            Spacer(Modifier.height(24.dp))

            Text(
                "ПРИ ВВОДЕ ЦИФРЫ В КЛЕТКУ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            NoteClearOption(
                title = "Очищать только клетку",
                description = "Пометки в этой клетке удаляются. В остальных клетках не трогаются.",
                selected = noteClearMode == NoteClearMode.CLEAR_CELL,
                onClick = { viewModel.setNoteClearMode(NoteClearMode.CLEAR_CELL) }
            )
            NoteClearOption(
                title = "Умная очистка",
                description = "Пометки в этой клетке удаляются, и введённая цифра убирается как пометка из клеток той же строки, столбца и региона.",
                selected = noteClearMode == NoteClearMode.CLEAR_PEERS,
                onClick = { viewModel.setNoteClearMode(NoteClearMode.CLEAR_PEERS) }
            )
            NoteClearOption(
                title = "Не трогать",
                description = "Пометки в клетке сохраняются и просто скрываются, пока в ней стоит цифра. Если цифру стереть, пометки вернутся.",
                selected = noteClearMode == NoteClearMode.KEEP,
                onClick = { viewModel.setNoteClearMode(NoteClearMode.KEEP) }
            )
        }
    }
}

@Composable
private fun NoteClearOption(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(shape)
            .background(if (selected) primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(1.dp, if (selected) primary else GridLineColor.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) primary else MaterialTheme.colorScheme.onBackground
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
