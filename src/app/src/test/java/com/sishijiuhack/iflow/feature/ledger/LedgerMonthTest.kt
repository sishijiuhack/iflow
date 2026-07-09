package com.sishijiuhack.iflow.feature.ledger

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerMonthTest {
    @Test
    fun toLedgerMonthLabel_formatsChineseYearMonth() {
        assertEquals("2026年7月", YearMonth.of(2026, 7).toLedgerMonthLabel())
    }

    @Test
    fun ledgerMonthNavigationCrossesYearBoundary() {
        assertEquals(
            YearMonth.of(2025, 12),
            YearMonth.of(2026, 1).previousLedgerMonth(),
        )
        assertEquals(
            YearMonth.of(2026, 1),
            YearMonth.of(2025, 12).nextLedgerMonth(),
        )
    }
}
