package com.zoya.sudoku.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.PuzzleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(puzzleRepository: PuzzleRepository) : ViewModel() {

    /** Only the single most recent puzzle is ever kept, so this is a plain yes/no. */
    val hasActivePuzzle: StateFlow<Boolean> = puzzleRepository.observeCurrent()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
