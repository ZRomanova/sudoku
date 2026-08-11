package com.zoya.sudoku.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.PuzzleRepository
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import com.zoya.sudoku.engine.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val regionLayoutRepository: RegionLayoutRepository
) : ViewModel() {

    /** Only the single most recent puzzle is ever kept, so this is a plain yes/no. */
    val hasActivePuzzle: StateFlow<Boolean> = puzzleRepository.observeCurrent()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _isStartingRandom = MutableStateFlow(false)
    val isStartingRandom: StateFlow<Boolean> = _isStartingRandom

    /**
     * Picks a random saved layout and starts a fresh puzzle on it at [difficulty]. Always has
     * something to play: if the user deleted every saved layout (including the seeded classic
     * one), the repository re-seeds it before picking.
     */
    fun playRandom(difficulty: Difficulty, onReady: () -> Unit) {
        if (_isStartingRandom.value) return
        viewModelScope.launch {
            _isStartingRandom.value = true
            withContext(Dispatchers.Default) {
                val layoutId = regionLayoutRepository.randomLayoutId()
                puzzleRepository.generateAndStartNew(layoutId, difficulty)
            }
            _isStartingRandom.value = false
            onReady()
        }
    }
}
