package com.zoya.sudoku.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.PuzzleRepository
import com.zoya.sudoku.data.repository.RegionLayoutRepository
import com.zoya.sudoku.data.repository.SavedLayout
import com.zoya.sudoku.engine.Difficulty
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

    /** Difficulty is chosen right here, right before generation - never baked into the layout. */
    fun play(layoutId: Long, difficulty: Difficulty, onReady: () -> Unit) {
        if (_generatingLayoutId.value != null) return
        viewModelScope.launch {
            _generatingLayoutId.value = layoutId
            withContext(Dispatchers.Default) { puzzleRepository.generateAndStartNew(layoutId, difficulty) }
            _generatingLayoutId.value = null
            onReady()
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
