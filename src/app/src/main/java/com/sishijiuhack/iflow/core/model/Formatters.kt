package com.sishijiuhack.iflow.core.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ledgerTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm", Locale.CHINA)
private val editableTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CHINA)

fun Long.formatLedgerTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
    return Instant.ofEpochMilli(this).atZone(zoneId).format(ledgerTimeFormatter)
}

fun Long.formatEditableTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
    return Instant.ofEpochMilli(this).atZone(zoneId).format(editableTimeFormatter)
}

fun parseEditableTime(input: String, zoneId: ZoneId = ZoneId.systemDefault()): Long? {
    return runCatching {
        java.time.LocalDateTime.parse(input.trim(), editableTimeFormatter)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

fun Long.formatSignedMoney(typeName: String): String {
    val prefix = if (typeName == "Income") "+" else "-"
    return prefix + MoneyCents(this).format()
}
