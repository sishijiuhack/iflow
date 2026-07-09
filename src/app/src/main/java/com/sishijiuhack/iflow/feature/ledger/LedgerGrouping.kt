package com.sishijiuhack.iflow.feature.ledger

import com.sishijiuhack.iflow.data.repository.TransactionListItem
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class LedgerDateGroup(
    val label: String,
    val transactions: List<TransactionListItem>,
)

fun groupTransactionsByDate(
    transactions: List<TransactionListItem>,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<LedgerDateGroup> {
    if (transactions.isEmpty()) return emptyList()

    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    return transactions
        .groupBy { transaction ->
            Instant.ofEpochMilli(transaction.occurredAt).atZone(zoneId).toLocalDate()
        }
        .toSortedMap(compareByDescending { it })
        .map { (date, groupTransactions) ->
            LedgerDateGroup(
                label = date.toLedgerGroupLabel(today),
                transactions = groupTransactions,
            )
        }
}

private fun LocalDate.toLedgerGroupLabel(today: LocalDate): String {
    return when {
        this == today -> "今天"
        this == today.minusDays(1) -> "昨天"
        isInSameWeekAs(today) -> dayOfWeek.toChineseLabel()
        year == today.year -> "${monthValue}月${dayOfMonth}日"
        else -> "${year}年${monthValue}月${dayOfMonth}日"
    }
}

private fun LocalDate.isInSameWeekAs(other: LocalDate): Boolean {
    return with(DayOfWeek.MONDAY) {
        val start = other.minusDays(((other.dayOfWeek.value - value) + 7).toLong() % 7)
        this@isInSameWeekAs >= start && this@isInSameWeekAs <= start.plusDays(6)
    }
}

private fun DayOfWeek.toChineseLabel(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
}
