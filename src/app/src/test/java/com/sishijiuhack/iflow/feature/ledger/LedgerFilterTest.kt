package com.sishijiuhack.iflow.feature.ledger

import com.sishijiuhack.iflow.data.repository.TransactionListItem
import com.sishijiuhack.iflow.domain.model.TransactionSource
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerFilterTest {
    @Test
    fun filterTransactions_matchesQueryAcrossBasicFields() {
        val result = filterTransactions(sampleTransactions, "咖啡", LedgerTypeFilter.All)

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun filterTransactions_matchesQueryByAmount() {
        val result = filterTransactions(sampleTransactions, "18.00", LedgerTypeFilter.All)

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun filterTransactions_filtersByType() {
        val result = filterTransactions(sampleTransactions, "", LedgerTypeFilter.Income)

        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test
    fun filterTransactions_combinesQueryAndType() {
        val result = filterTransactions(sampleTransactions, "现金", LedgerTypeFilter.Expense)

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun filterTransactions_filtersByAccount() {
        val result = filterTransactions(
            transactions = sampleTransactions,
            query = "",
            typeFilter = LedgerTypeFilter.All,
            accountId = 20L,
        )

        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test
    fun filterTransactions_filtersByCategory() {
        val result = filterTransactions(
            transactions = sampleTransactions,
            query = "",
            typeFilter = LedgerTypeFilter.All,
            categoryId = 10L,
        )

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun filterTransactions_filtersToday() {
        val result = filterTransactions(
            transactions = sampleTransactions,
            query = "",
            typeFilter = LedgerTypeFilter.All,
            dateFilter = LedgerDateFilter.Today,
            nowMillis = fixedNow,
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun filterTransactions_filtersLast7Days() {
        val result = filterTransactions(
            transactions = sampleTransactions,
            query = "",
            typeFilter = LedgerTypeFilter.All,
            dateFilter = LedgerDateFilter.Last7Days,
            nowMillis = fixedNow,
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun filterTransactions_filtersThisMonth() {
        val result = filterTransactions(
            transactions = sampleTransactions,
            query = "",
            typeFilter = LedgerTypeFilter.All,
            dateFilter = LedgerDateFilter.ThisMonth,
            nowMillis = fixedNow,
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(listOf(1L, 2L), result.map { it.id })
    }

    private val fixedNow = millis(2026, 7, 8, 12, 0)

    private val sampleTransactions = listOf(
        TransactionListItem(
            id = 1L,
            type = TransactionType.Expense,
            amountCents = 1800L,
            categoryId = 10L,
            categoryName = "餐饮",
            accountId = 19L,
            accountName = "现金",
            merchant = "咖啡店",
            note = "早餐",
            occurredAt = millis(2026, 7, 8, 9, 30),
            source = TransactionSource.Manual,
            status = TransactionStatus.Confirmed,
        ),
        TransactionListItem(
            id = 2L,
            type = TransactionType.Income,
            amountCents = 500000L,
            categoryId = 11L,
            categoryName = "工资",
            accountId = 20L,
            accountName = "银行卡",
            merchant = null,
            note = "七月工资",
            occurredAt = millis(2026, 7, 1, 18, 0),
            source = TransactionSource.Manual,
            status = TransactionStatus.Confirmed,
        ),
    )

    private fun millis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Long {
        return LocalDateTime.of(year, month, day, hour, minute)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }
}
