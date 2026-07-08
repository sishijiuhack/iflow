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
import org.junit.Assert.assertFalse
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

    @Test
    fun toCsv_prefixesUtf8BomBeforeHeader() {
        val csv = exporter.toCsv(sampleSnapshot())

        assertTrue(csv.startsWith("\uFEFFid,type,amountCents"))
    }

    @Test
    fun toCsv_escapesQuotesAndNewlines() {
        val csv = exporter.toCsv(sampleSnapshotWithSpecialText())

        assertTrue(csv.contains("\"Store \"\"A\"\"\""))
        assertTrue(csv.contains("\"line1\nline2\\tail\tend\""))
    }

    @Test
    fun toCsv_neutralizesFormulaLikeTextCells() {
        val snapshot = sampleSnapshot().let {
            it.copy(
                transactions = listOf(
                    it.transactions.first().copy(
                        merchant = "=SUM(A1:A2)",
                        note = "@cmd",
                    ),
                ),
            )
        }

        val csv = exporter.toCsv(snapshot)

        assertTrue(csv.contains("'=SUM(A1:A2)"))
        assertTrue(csv.contains("'@cmd"))
    }

    @Test
    fun toCsv_neutralizesFullWidthFormulaLikeTextCells() {
        val snapshot = sampleSnapshot().let {
            it.copy(
                transactions = listOf(
                    it.transactions.first().copy(
                        merchant = "＝SUM(A1:A2)",
                        note = "＠cmd",
                    ),
                ),
            )
        }

        val csv = exporter.toCsv(snapshot)

        assertTrue(csv.contains("'＝SUM(A1:A2)"))
        assertTrue(csv.contains("'＠cmd"))
    }

    @Test
    fun toCsv_neutralizesFormulaLikeTextAfterLeadingWhitespace() {
        val snapshot = sampleSnapshot().let {
            it.copy(
                transactions = listOf(
                    it.transactions.first().copy(
                        merchant = " =SUM(A1:A2)",
                        note = "\t@cmd",
                    ),
                ),
            )
        }

        val csv = exporter.toCsv(snapshot)

        assertTrue(csv.contains("' =SUM(A1:A2)"))
        assertTrue(csv.contains("'\t@cmd"))
    }

    @Test
    fun toCsv_neutralizesFormulaLikeCategoryAndAccountNames() {
        val snapshot = sampleSnapshot().let {
            it.copy(
                categories = listOf(it.categories.first().copy(name = "=Category")),
                accounts = listOf(it.accounts.first().copy(name = "+Account")),
            )
        }

        val csv = exporter.toCsv(snapshot)

        assertTrue(csv.contains("'=Category"))
        assertTrue(csv.contains("'+Account"))
    }

    @Test
    fun toCsv_neutralizesFormulaLikeRawNotificationId() {
        val snapshot = sampleSnapshot().let {
            it.copy(
                transactions = listOf(
                    it.transactions.first().copy(rawNotificationId = "=raw-id"),
                ),
            )
        }

        val csv = exporter.toCsv(snapshot)

        assertTrue(csv.contains("'=raw-id"))
    }

    @Test
    fun toCsv_exportsConfirmedTransactionsOnly() {
        val snapshot = sampleSnapshot().let {
            it.copy(
                transactions = listOf(
                    it.transactions.first().copy(
                        id = 1L,
                        merchant = "Confirmed Store",
                        status = TransactionStatus.Confirmed,
                    ),
                    it.transactions.first().copy(
                        id = 2L,
                        merchant = "Pending Store",
                        status = TransactionStatus.Pending,
                    ),
                ),
            )
        }

        val csv = exporter.toCsv(snapshot)

        assertTrue(csv.contains("Confirmed Store"))
        assertFalse(csv.contains("Pending Store"))
    }

    @Test
    fun toJson_includesPendingTransactionsForBackup() {
        val snapshot = sampleSnapshot().let {
            it.copy(
                transactions = listOf(
                    it.transactions.first().copy(
                        id = 1L,
                        merchant = "Confirmed Store",
                        status = TransactionStatus.Confirmed,
                    ),
                    it.transactions.first().copy(
                        id = 2L,
                        merchant = "Pending Store",
                        status = TransactionStatus.Pending,
                    ),
                ),
            )
        }

        val json = exporter.toJson(snapshot)

        assertTrue(json.contains("Confirmed Store"))
        assertTrue(json.contains("Pending Store"))
        assertTrue(json.contains("\"status\": \"Pending\""))
    }

    @Test
    fun toJson_escapesSpecialCharacters() {
        val json = exporter.toJson(sampleSnapshotWithSpecialText())

        assertTrue(json.contains("\\\"A\\\""))
        assertTrue(json.contains("line1\\nline2\\\\tail\\tend"))
        assertTrue(json.contains("raw\\\\id"))
    }

    @Test
    fun toJson_escapesControlCharacters() {
        val snapshot = sampleSnapshot().let {
            it.copy(
                transactions = listOf(
                    it.transactions.first().copy(
                        merchant = "Store\bForm\u000C",
                        note = "hidden\u001Fcontrol",
                    ),
                ),
            )
        }

        val json = exporter.toJson(snapshot)

        assertTrue(json.contains("Store\\bForm\\f"))
        assertTrue(json.contains("hidden\\u001fcontrol"))
    }

    @Test
    fun toJson_escapesNotificationRuleKeywords() {
        val snapshot = sampleSnapshot().let {
            it.copy(
                notificationRules = listOf(
                    it.notificationRules.first().copy(
                        keywords = listOf("付款\"确认", "收款\\到账"),
                    ),
                ),
            )
        }

        val json = exporter.toJson(snapshot)

        assertTrue(json.contains("\"keywords\": [\"付款\\\"确认\", \"收款\\\\到账\"]"))
    }

    private fun sampleSnapshotWithSpecialText(): LedgerExportSnapshot {
        val snapshot = sampleSnapshot()
        val transaction = snapshot.transactions.first().copy(
            merchant = "Store \"A\"",
            note = "line1\nline2\\tail\tend",
            rawNotificationId = "raw\\id",
        )

        return snapshot.copy(transactions = listOf(transaction))
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
