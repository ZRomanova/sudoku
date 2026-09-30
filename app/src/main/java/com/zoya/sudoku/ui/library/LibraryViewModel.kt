package com.zoya.sudoku.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.PuzzleRepository
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import com.zoya.sudoku.data.repository.SavedLayout
import com.zoya.sudoku.engine.Difficulty
import com.zoya.sudoku.engine.PuzzleGenerationException
import com.zoya.sudoku.ui.capitalizeFirst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(
    private val layoutRepository: RegionLayoutRepository,
    private val puzzleRepository: PuzzleRepository
) : ViewModel() {

    val layouts: StateFlow<List<SavedLayout>> = layoutRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _generatingLayoutId = MutableStateFlow<Long?>(null)
    val generatingLayoutId: StateFlow<Long?> = _generatingLayoutId

    private val _generationFailed = MutableStateFlow(false)
    val generationFailed: StateFlow<Boolean> = _generationFailed

    fun dismissGenerationFailed() {
        _generationFailed.value = false
    }

    /** Difficulty is chosen right here, right before generation - never baked into the layout.
     *  Always starts a new puzzle, even if this layout already has one in progress. */
    fun play(layoutId: Long, difficulty: Difficulty, onReady: (Long) -> Unit) {
        if (_generatingLayoutId.value != null) return
        viewModelScope.launch {
            _generatingLayoutId.value = layoutId
            val puzzleId = try {
                withContext(Dispatchers.Default) { puzzleRepository.generateAndStartNew(layoutId, difficulty) }
            } catch (e: PuzzleGenerationException) {
                _generationFailed.value = true
                null
            } finally {
                _generatingLayoutId.value = null
            }
            puzzleId?.let(onReady)
        }
    }

    fun rename(layoutId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch { layoutRepository.rename(layoutId, trimmed.capitalizeFirst()) }
    }

    fun delete(layoutId: Long) {
        viewModelScope.launch { layoutRepository.delete(layoutId) }
    }
}
