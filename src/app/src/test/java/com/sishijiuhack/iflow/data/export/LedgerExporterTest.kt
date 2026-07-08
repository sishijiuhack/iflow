package com.sishijiuhack.iflow.data.export

import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.local.entity.TransactionEntity
import com.sishijiuhack.iflow.data.repository.LedgerExportSnapshot
import com.sishijiuhack.iflow.domain.model.AccountType
import com.sishijiuhack.iflow.domain.model.TransactionSource
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerExporterTest {
    private val exporter = LedgerExporter()

    @Test
    fun toJson_containsCoreFields() {
        val json = exporter.toJson(sampleSnapshot())

        assertTrue(json.contains("\"transactions\""))
        assertTrue(json.contains("\"amountCents\": 1200"))
        assertTrue(json.contains("\"categories\""))
        assertTrue(json.contains("\"accounts\""))
    }

    @Test
    fun toCsv_escapesCommaInNote() {
        val csv = exporter.toCsv(sampleSnapshot())

        assertTrue(csv.contains("id,type,amountCents"))
        assertTrue(csv.contains("\"hello,world\""))
    }

    private fun sampleSnapshot(): LedgerExportSnapshot {
        return LedgerExportSnapshot(
            exportedAt = 1000L,
            categories = listOf(
                CategoryEntity(id = 1L, name = "餐饮", type = TransactionType.Expense, sortOrder = 1, isDefault = true),
            ),
            accounts = listOf(
                AccountEntity(id = 1L, name = "现金", type = AccountType.Cash, sortOrder = 1, isDefault = true),
            ),
            transactions = listOf(
                TransactionEntity(
                    id = 1L,
                    type = TransactionType.Expense,
                    amountCents = 1200L,
                    categoryId = 1L,
                    accountId = 1L,
                    merchant = "店",
                    note = "hello,world",
                    occurredAt = 1000L,
                    source = TransactionSource.Manual,
                    status = TransactionStatus.Confirmed,
                    rawNotificationId = null,
                    createdAt = 1000L,
                    updatedAt = 1000L,
                ),
            ),
            settings = null,
        )
    }
}
