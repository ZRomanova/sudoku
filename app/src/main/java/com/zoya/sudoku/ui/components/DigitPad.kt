package com.zoya.sudoku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DigitPad(onDigit: (Int) -> Unit, onErase: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 3) {
                    val digit = row * 3 + col + 1
                    OutlinedButton(onClick = { onDigit(digit) }, modifier = Modifier.weight(1f)) {
                        Text("$digit")
                    }
                }
            }
        }
        OutlinedButton(
            onClick = onErase,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text("Стереть")
        }
    }
}
