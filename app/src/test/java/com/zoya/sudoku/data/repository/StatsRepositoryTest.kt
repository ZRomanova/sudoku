package com.zoya.sudoku.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsRepositoryTest {

    @Test
    fun `percent is zero with no finished games, never divides by zero`() {
        assertEquals(0, ResultTally(correct = 0, total = 0).percent)
    }

    @Test
    fun `percent rounds to nearest integer`() {
        assertEquals(67, ResultTally(correct = 2, total = 3).percent) // 66.67 -> 67
        assertEquals(33, ResultTally(correct = 1, total = 3).percent) // 33.33 -> 33
        assertEquals(50, ResultTally(correct = 1, total = 2).percent)
    }

    @Test
    fun `enough correct games can round up to 100 percent without a perfect record`() {
        assertEquals(100, ResultTally(correct = 995, total = 1000).percent) // 99.5 -> 100
    }

    @Test
    fun `a genuinely perfect record is 100 percent`() {
        assertEquals(100, ResultTally(correct = 5, total = 5).percent)
    }
}
