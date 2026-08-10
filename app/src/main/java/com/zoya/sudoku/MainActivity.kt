package com.zoya.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zoya.sudoku.ui.navigation.SudokuNavHost
import com.zoya.sudoku.ui.theme.SudokuRegionsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SudokuApp).container

        setContent {
            SudokuRegionsTheme {
                // Home is always the landing screen; an in-progress puzzle (only the last one is
                // ever kept) shows up there as a "Продолжить" button instead of being auto-opened.
                var ready by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    container.regionLayoutRepository.ensureDefaultLayoutExists()
                    ready = true
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (ready) {
                        SudokuNavHost(container = container)
                    }
                }
            }
        }
    }
}
