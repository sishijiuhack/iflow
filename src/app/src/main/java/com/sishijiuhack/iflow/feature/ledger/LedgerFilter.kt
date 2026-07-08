package com.sishijiuhack.iflow.feature.ledger

import com.sishijiuhack.iflow.data.repository.TransactionListItem
import com.sishijiuhack.iflow.domain.model.TransactionType

enum class LedgerTypeFilter {
    All,
    Expense,
    Income,
}

fun filterTransactions(
    transactions: List<TransactionListItem>,
    query: String,
    typeFilter: LedgerTypeFilter,
): List<TransactionListItem> {
    val normalizedQuery = query.trim()
    return transactions.filter { transaction ->
        val matchesType = when (typeFilter) {
            LedgerTypeFilter.All -> true
            LedgerTypeFilter.Expense -> transaction.type == TransactionType.Expense
            LedgerTypeFilter.Income -> transaction.type == TransactionType.Income
        }
        val matchesQuery = normalizedQuery.isBlank() ||
            listOf(
                transaction.categoryName,
                transaction.accountName,
                transaction.merchant.orEmpty(),
                transaction.note.orEmpty(),
            ).any { it.contains(normalizedQuery, ignoreCase = true) }
        matchesType && matchesQuery
    }
}
