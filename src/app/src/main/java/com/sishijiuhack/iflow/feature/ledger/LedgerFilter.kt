package com.sishijiuhack.iflow.feature.ledger

import com.sishijiuhack.iflow.data.repository.TransactionListItem
import com.sishijiuhack.iflow.core.model.MoneyCents
import com.sishijiuhack.iflow.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

enum class LedgerTypeFilter {
    All,
    Expense,
    Income,
}

enum class LedgerDateFilter {
    All,
    Today,
    Last7Days,
    ThisMonth,
}

fun filterTransactions(
    transactions: List<TransactionListItem>,
    query: String,
    typeFilter: LedgerTypeFilter,
    dateFilter: LedgerDateFilter = LedgerDateFilter.All,
    accountId: Long? = null,
    categoryId: Long? = null,
    selectedMonth: YearMonth? = null,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<TransactionListItem> {
    val normalizedQuery = query.normalizeLedgerSearchQuery()
    val dateRange = dateFilter.toMillisRange(nowMillis, zoneId)
    val monthRange = selectedMonth?.toMillisRange(zoneId)
    return transactions.filter { transaction ->
        val matchesType = when (typeFilter) {
            LedgerTypeFilter.All -> true
            LedgerTypeFilter.Expense -> transaction.type == TransactionType.Expense
            LedgerTypeFilter.Income -> transaction.type == TransactionType.Income
        }
        val matchesDate = (dateRange == null || transaction.occurredAt in dateRange) &&
            (monthRange == null || transaction.occurredAt in monthRange)
        val matchesAccount = accountId == null || transaction.accountId == accountId
        val matchesCategory = categoryId == null || transaction.categoryId == categoryId
        val matchesQuery = normalizedQuery.isBlank() ||
            listOf(
                transaction.categoryName,
                transaction.accountName,
                transaction.merchant.orEmpty(),
                transaction.note.orEmpty(),
                MoneyCents(transaction.amountCents).format(),
                transaction.amountCents.toPlainAmountText(),
                transaction.amountCents.toSignedPlainAmountText(transaction.type),
            ).any { it.contains(normalizedQuery, ignoreCase = true) }
        matchesType && matchesDate && matchesAccount && matchesCategory && matchesQuery
    }
}

private fun YearMonth.toMillisRange(zoneId: ZoneId): LongRange {
    val start = atDay(1).startMillis(zoneId)
    val endExclusive = plusMonths(1).atDay(1).startMillis(zoneId)
    return start..<endExclusive
}

private fun String.normalizeLedgerSearchQuery(): String {
    val normalized = trim().map { char ->
        when (char) {
            in '０'..'９' -> '0' + (char - '０')
            '．', '。' -> '.'
            '＋' -> '+'
            '－', '—', 'ー' -> '-'
            '，' -> ','
            else -> char
        }
    }.joinToString(separator = "")
    val currencyStripped = normalized
        .replace(Regex("人民币|rmb|cny", RegexOption.IGNORE_CASE), "")
        .filterNot {
            it == '¥' ||
                it == '￥' ||
                it == '元' ||
                it == '块' ||
                it == '整' ||
                it == ',' ||
                it.isWhitespace() ||
                Character.isSpaceChar(it)
        }
        .trim()
    return if (currencyStripped.any { it.isDigit() }) currencyStripped else normalized
}

private fun Long.toPlainAmountText(): String {
    val yuan = this / 100
    val cents = (this % 100).toString().padStart(2, '0')
    return "$yuan.$cents"
}

private fun Long.toSignedPlainAmountText(type: TransactionType): String {
    val sign = when (type) {
        TransactionType.Expense -> "-"
        TransactionType.Income -> "+"
    }
    return sign + toPlainAmountText()
}

private fun LedgerDateFilter.toMillisRange(
    nowMillis: Long,
    zoneId: ZoneId,
): LongRange? {
    if (this == LedgerDateFilter.All) return null
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val startDate = when (this) {
        LedgerDateFilter.All -> return null
        LedgerDateFilter.Today -> today
        LedgerDateFilter.Last7Days -> today.minusDays(6)
        LedgerDateFilter.ThisMonth -> today.withDayOfMonth(1)
    }
    val endExclusive = today.plusDays(1).startMillis(zoneId)
    return startDate.startMillis(zoneId)..<endExclusive
}

private fun LocalDate.startMillis(zoneId: ZoneId): Long {
    return atStartOfDay(zoneId).toInstant().toEpochMilli()
}
