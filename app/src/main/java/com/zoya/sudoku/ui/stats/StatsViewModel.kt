package com.zoya.sudoku.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoya.sudoku.data.repository.LayoutStats
import com.zoya.sudoku.data.repository.StatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsViewModel(private val statsRepository: StatsRepository) : ViewModel() {

    /** Best-performing layouts first, so "which layout works well" is answered by just scanning
     *  from the top - no separate sort control needed. */
    val layoutStats: StateFlow<List<LayoutStats>> = statsRepository.observeLayoutStats()
        .map { stats -> stats.sortedByDescending { it.overall.percent } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun reset() {
        viewModelScope.launch { statsRepository.reset() }
    }
}
