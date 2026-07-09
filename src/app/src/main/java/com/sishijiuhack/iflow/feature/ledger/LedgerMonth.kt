package com.sishijiuhack.iflow.feature.ledger

import java.time.YearMonth

fun YearMonth.toLedgerMonthLabel(): String {
    return "${year}年${monthValue}月"
}

fun YearMonth.previousLedgerMonth(): YearMonth = minusMonths(1)

fun YearMonth.nextLedgerMonth(): YearMonth = plusMonths(1)
