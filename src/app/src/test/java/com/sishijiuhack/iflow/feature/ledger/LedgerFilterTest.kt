package com.sishijiuhack.iflow.feature.ledger

import com.sishijiuhack.iflow.data.repository.TransactionListItem
import com.sishijiuhack.iflow.domain.model.TransactionSource
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerFilterTest {
    @Test
    fun filterTransactions_matchesQueryAcrossBasicFields() {
        val result = filterTransactions(sampleTransactions, "咖啡", LedgerTypeFilter.All)

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

    private val sampleTransactions = listOf(
        TransactionListItem(
            id = 1L,
            type = TransactionType.Expense,
            amountCents = 1800L,
            categoryName = "餐饮",
            accountName = "现金",
            merchant = "咖啡店",
            note = "早餐",
            occurredAt = 1000L,
            source = TransactionSource.Manual,
            status = TransactionStatus.Confirmed,
        ),
        TransactionListItem(
            id = 2L,
            type = TransactionType.Income,
            amountCents = 500000L,
            categoryName = "工资",
            accountName = "银行卡",
            merchant = null,
            note = "七月工资",
            occurredAt = 2000L,
            source = TransactionSource.Manual,
            status = TransactionStatus.Confirmed,
        ),
    )
}
