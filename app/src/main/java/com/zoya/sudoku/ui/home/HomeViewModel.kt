package com.zoya.sudoku.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.InProgressPuzzle
import com.zoya.sudoku.data.repository.PuzzleRepository
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import com.zoya.sudoku.engine.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val regionLayoutRepository: RegionLayoutRepository
) : ViewModel() {

    /** Most recently touched first; the Home screen shows a handful and links to the full list. */
    val inProgress: StateFlow<List<InProgressPuzzle>> = puzzleRepository.observeInProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isStartingRandom = MutableStateFlow(false)
    val isStartingRandom: StateFlow<Boolean> = _isStartingRandom

    /**
     * Picks a random saved layout and starts a fresh puzzle on it at [difficulty] - always a new
     * puzzle, never overwriting a run already in progress on that layout. Always has something to
     * play: if the user deleted every saved layout (including the seeded classic one), the
     * repository re-seeds it before picking.
     */
    fun playRandom(difficulty: Difficulty, onReady: (Long) -> Unit) {
        if (_isStartingRandom.value) return
        viewModelScope.launch {
            _isStartingRandom.value = true
            val puzzleId = withContext(Dispatchers.Default) {
                val layoutId = regionLayoutRepository.randomLayoutId()
                puzzleRepository.generateAndStartNew(layoutId, difficulty)
            }
            _isStartingRandom.value = false
            onReady(puzzleId)
        }
    }
}
