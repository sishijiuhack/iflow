package com.sishijiuhack.iflow.core.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ledgerTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm", Locale.CHINA)
private val editableTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CHINA)
private val exportFileTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.CHINA)

fun Long.formatLedgerTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
    return Instant.ofEpochMilli(this).atZone(zoneId).format(ledgerTimeFormatter)
}

fun Long.formatEditableTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
    return Instant.ofEpochMilli(this).atZone(zoneId).format(editableTimeFormatter)
}

fun Long.formatExportFileTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
    return Instant.ofEpochMilli(this).atZone(zoneId).format(exportFileTimeFormatter)
}

fun parseEditableTime(input: String, zoneId: ZoneId = ZoneId.systemDefault()): Long? {
    return runCatching {
        java.time.LocalDateTime.parse(input.normalizeEditableTimeInput(), editableTimeFormatter)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun String.normalizeEditableTimeInput(): String {
    return trim().map { char ->
        when (char) {
            in '０'..'９' -> '0' + (char - '０')
            '－', '—', 'ー' -> '-'
            '：' -> ':'
            '　' -> ' '
            '年', '月' -> '-'
            '日' -> ' '
            '时' -> ':'
            '分' -> ' '
            else -> char
        }
    }.joinToString(separator = "")
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun Long.formatSignedMoney(typeName: String): String {
    val prefix = if (typeName == "Income") "+" else "-"
    return prefix + MoneyCents(this).format()
}
