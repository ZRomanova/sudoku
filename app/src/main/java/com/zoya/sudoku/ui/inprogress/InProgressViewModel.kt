package com.zoya.sudoku.ui.inprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.InProgressPuzzle
import com.zoya.sudoku.data.repository.PuzzleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InProgressViewModel(private val puzzleRepository: PuzzleRepository) : ViewModel() {

    val puzzles: StateFlow<List<InProgressPuzzle>> = puzzleRepository.observeInProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Abandons a puzzle without recording a stats result - it's simply dropped, same as Finish
     *  does internally, just without the "player actually completed it" bookkeeping. */
    fun abandon(id: Long) {
        viewModelScope.launch { puzzleRepository.finishCurrent(id) }
    }
}
