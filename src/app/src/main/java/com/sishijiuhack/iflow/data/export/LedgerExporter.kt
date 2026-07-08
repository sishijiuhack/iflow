package com.sishijiuhack.iflow.data.export

import com.sishijiuhack.iflow.data.repository.LedgerExportSnapshot

class LedgerExporter {
    fun toJson(snapshot: LedgerExportSnapshot): String {
        return buildString {
            appendLine("{")
            appendLine("  \"exportedAt\": ${snapshot.exportedAt},")
            appendLine("  \"categories\": [")
            snapshot.categories.forEachIndexed { index, category ->
                append("    {")
                append("\"id\": ${category.id}, ")
                append("\"name\": \"${category.name.escapeJson()}\", ")
                append("\"type\": \"${category.type}\", ")
                append("\"icon\": ${category.icon?.let { "\"${it.escapeJson()}\"" } ?: "null"}, ")
                append("\"sortOrder\": ${category.sortOrder}, ")
                append("\"isDefault\": ${category.isDefault}")
                append("}")
                appendLine(if (index == snapshot.categories.lastIndex) "" else ",")
            }
            appendLine("  ],")
            appendLine("  \"accounts\": [")
            snapshot.accounts.forEachIndexed { index, account ->
                append("    {")
                append("\"id\": ${account.id}, ")
                append("\"name\": \"${account.name.escapeJson()}\", ")
                append("\"type\": \"${account.type}\", ")
                append("\"sortOrder\": ${account.sortOrder}, ")
                append("\"isDefault\": ${account.isDefault}")
                append("}")
                appendLine(if (index == snapshot.accounts.lastIndex) "" else ",")
            }
            appendLine("  ],")
            appendLine("  \"notificationRules\": [")
            snapshot.notificationRules.forEachIndexed { index, rule ->
                append("    {")
                append("\"id\": ${rule.id}, ")
                append("\"packageName\": \"${rule.packageName.escapeJson()}\", ")
                append("\"appName\": \"${rule.appName.escapeJson()}\", ")
                append("\"enabled\": ${rule.enabled}, ")
                append("\"keywords\": [${rule.keywords.joinToString(", ") { "\"${it.escapeJson()}\"" }}], ")
                append("\"amountPattern\": \"${rule.amountPattern.escapeJson()}\", ")
                append("\"directionPattern\": \"${rule.directionPattern.escapeJson()}\", ")
                append("\"merchantPattern\": ${rule.merchantPattern?.let { "\"${it.escapeJson()}\"" } ?: "null"}")
                append("}")
                appendLine(if (index == snapshot.notificationRules.lastIndex) "" else ",")
            }
            appendLine("  ],")
            appendLine("  \"transactions\": [")
            snapshot.transactions.forEachIndexed { index, transaction ->
                append("    {")
                append("\"id\": ${transaction.id}, ")
                append("\"type\": \"${transaction.type}\", ")
                append("\"amountCents\": ${transaction.amountCents}, ")
                append("\"categoryId\": ${transaction.categoryId}, ")
                append("\"accountId\": ${transaction.accountId}, ")
                append("\"merchant\": ${transaction.merchant?.let { "\"${it.escapeJson()}\"" } ?: "null"}, ")
                append("\"note\": ${transaction.note?.let { "\"${it.escapeJson()}\"" } ?: "null"}, ")
                append("\"occurredAt\": ${transaction.occurredAt}, ")
                append("\"source\": \"${transaction.source}\", ")
                append("\"status\": \"${transaction.status}\", ")
                append("\"rawNotificationId\": ${transaction.rawNotificationId?.let { "\"${it.escapeJson()}\"" } ?: "null"}, ")
                append("\"createdAt\": ${transaction.createdAt}, ")
                append("\"updatedAt\": ${transaction.updatedAt}")
                append("}")
                appendLine(if (index == snapshot.transactions.lastIndex) "" else ",")
            }
            appendLine("  ],")
            appendLine("  \"settings\": ${snapshot.settings?.let { setting ->
                "{\"autoCaptureEnabled\": ${setting.autoCaptureEnabled}, \"autoConfirmEnabled\": ${setting.autoConfirmEnabled}, \"defaultAccountId\": ${setting.defaultAccountId}, \"lastExportedAt\": ${setting.lastExportedAt}}"
            } ?: "null"}")
            appendLine("}")
        }
    }

    fun toCsv(snapshot: LedgerExportSnapshot): String {
        return buildString {
            appendLine("id,type,amountCents,category,account,merchant,note,occurredAt,source,status")
            val categoryMap = snapshot.categories.associateBy { it.id }
            val accountMap = snapshot.accounts.associateBy { it.id }
            snapshot.transactions.forEach { transaction ->
                appendLine(
                    listOf(
                        transaction.id.toString(),
                        transaction.type.toString(),
                        transaction.amountCents.toString(),
                        categoryMap[transaction.categoryId]?.name.orEmpty(),
                        accountMap[transaction.accountId]?.name.orEmpty(),
                        transaction.merchant.orEmpty(),
                        transaction.note.orEmpty(),
                        transaction.occurredAt.toString(),
                        transaction.source.toString(),
                        transaction.status.toString(),
                    ).joinToString(",") { it.escapeCsv() },
                )
            }
        }
    }

    private fun String.escapeJson(): String {
        return buildString {
            this@escapeJson.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }
    }

    private fun String.escapeCsv(): String {
        val escaped = replace("\"", "\"\"")
        return if (escaped.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
