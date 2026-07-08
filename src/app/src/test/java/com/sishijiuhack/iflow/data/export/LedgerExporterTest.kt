package com.sishijiuhack.iflow.data.export

import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.AppSettingEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.local.entity.NotificationRuleEntity
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

        assertTrue(json.contains("\"schemaVersion\": 1"))
        assertTrue(json.contains("\"transactions\""))
        assertTrue(json.contains("\"amountCents\": 1200"))
        assertTrue(json.contains("\"categories\""))
        assertTrue(json.contains("\"accounts\""))
        assertTrue(json.contains("\"notificationRules\""))
        assertTrue(json.contains("\"packageName\": \"com.tencent.mm\""))
        assertTrue(json.contains("\"settings\""))
        assertTrue(json.contains("\"autoCaptureEnabled\": true"))
        assertTrue(json.contains("\"createdAt\": 1000"))
        assertTrue(json.contains("\"updatedAt\": 2000"))
    }

    @Test
    fun toCsv_escapesCommaInNote() {
        val csv = exporter.toCsv(sampleSnapshot())

        assertTrue(csv.contains("id,type,amountCents,category,account,merchant,note,occurredAt,source,status,rawNotificationId,createdAt,updatedAt"))
        assertTrue(csv.contains("\"hello,world\""))
        assertTrue(csv.contains("raw-1,1000,1000"))
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
            notificationRules = listOf(
                NotificationRuleEntity(
                    id = 1L,
                    packageName = "com.tencent.mm",
                    appName = "微信",
                    enabled = true,
                    keywords = listOf("付款", "收款"),
                    amountPattern = """(\d+(?:\.\d{1,2})?)元""",
                    directionPattern = "付款|收款",
                    merchantPattern = null,
                ),
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
                    rawNotificationId = "raw-1",
                    createdAt = 1000L,
                    updatedAt = 1000L,
                ),
            ),
            settings = AppSettingEntity(
                autoCaptureEnabled = true,
                autoConfirmEnabled = false,
                defaultAccountId = 1L,
                lastExportedAt = 2000L,
                createdAt = 1000L,
                updatedAt = 2000L,
            ),
        )
    }
}
