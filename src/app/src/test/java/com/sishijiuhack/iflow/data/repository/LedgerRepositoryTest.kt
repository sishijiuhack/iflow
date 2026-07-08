package com.sishijiuhack.iflow.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sishijiuhack.iflow.data.local.DefaultDataSeeder
import com.sishijiuhack.iflow.data.local.IFlowDatabase
import com.sishijiuhack.iflow.data.local.entity.NotificationRuleEntity
import com.sishijiuhack.iflow.domain.model.AccountType
import com.sishijiuhack.iflow.domain.model.TransactionSource
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType
import com.sishijiuhack.iflow.notification.PaymentNotificationParseResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class LedgerRepositoryTest {
    private lateinit var database: IFlowDatabase
    private lateinit var repository: LedgerRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IFlowDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = LedgerRepository(database, DefaultDataSeeder(database))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun savePendingNotificationTransaction_usesEnabledRules() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-1"))

        assertEquals(1, database.transactionDao().listActiveTransactions().size)
        assertTrue(insertedId != null)
    }

    @Test
    fun savePendingNotificationTransaction_ignoresDuplicateFingerprint() = runTest {
        val firstId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-duplicate"))
        val secondId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-duplicate"))

        assertTrue(firstId != null)
        assertNull(secondId)
        assertEquals(1, database.transactionDao().listActiveTransactions().size)
    }

    @Test
    fun pendingNotificationTransaction_canBeConfirmedAndSoftDeleted() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-lifecycle"))

        assertTrue(insertedId != null)
        assertEquals(
            TransactionStatus.Pending,
            database.transactionDao().getById(insertedId!!)?.status,
        )

        repository.confirmPendingTransaction(insertedId)

        assertEquals(
            TransactionStatus.Confirmed,
            database.transactionDao().getById(insertedId)?.status,
        )

        repository.softDeleteTransaction(insertedId)

        assertEquals(
            TransactionStatus.Deleted,
            database.transactionDao().getById(insertedId)?.status,
        )
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun observeTransactions_hidesPendingUntilConfirmed() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-ledger-pending"))

        assertTrue(insertedId != null)
        assertTrue(repository.observeTransactions().first().isEmpty())
        assertTrue(repository.observeRecentTransactions(limit = 5).first().isEmpty())

        repository.confirmPendingTransaction(insertedId!!)

        assertEquals(listOf(insertedId), repository.observeTransactions().first().map { it.id })
        assertEquals(listOf(insertedId), repository.observeRecentTransactions(limit = 5).first().map { it.id })
    }

    @Test
    fun confirmPendingTransaction_doesNotRestoreDeletedTransaction() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-confirm-deleted"))

        assertTrue(insertedId != null)
        repository.softDeleteTransaction(insertedId!!)
        repository.confirmPendingTransaction(insertedId)

        assertEquals(
            TransactionStatus.Deleted,
            database.transactionDao().getById(insertedId)?.status,
        )
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun softDeleteTransaction_doesNotTouchAlreadyDeletedTransaction() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-delete-idempotent"))

        assertTrue(insertedId != null)
        repository.softDeleteTransaction(insertedId!!)
        val deleted = database.transactionDao().getById(insertedId)

        Thread.sleep(2)
        repository.softDeleteTransaction(insertedId)

        assertEquals(deleted?.updatedAt, database.transactionDao().getById(insertedId)?.updatedAt)
        assertEquals(TransactionStatus.Deleted, database.transactionDao().getById(insertedId)?.status)
    }

    @Test
    fun savePendingNotificationTransaction_ignoresDisabledRule() = runTest {
        repository.ensureDefaultData()
        database.notificationRuleDao().listAll()
            .filter { it.packageName == "com.tencent.mm" }
            .forEach { rule ->
                database.notificationRuleDao().update(rule.copy(enabled = false))
            }

        val insertedId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-2"))

        assertNull(insertedId)
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun savePendingNotificationTransaction_requiresRuleKeyword() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-3").copy(
                rawTitle = "微信团队",
                rawText = "安全提醒 12.00元",
            ),
        )

        assertNull(insertedId)
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun savePendingNotificationTransaction_rejectsNonPositiveAmount() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-zero-notification").copy(amountCents = 0L),
        )

        assertNull(insertedId)
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun savePendingNotificationTransaction_respectsAutoCaptureSetting() = runTest {
        repository.setAutoCaptureEnabled(false)

        val insertedId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-auto-capture"))

        assertNull(insertedId)
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun savePendingNotificationTransaction_respectsAutoConfirmSetting() = runTest {
        repository.setAutoConfirmEnabled(true)

        val insertedId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-auto-confirm"))

        assertTrue(insertedId != null)
        assertEquals(
            TransactionStatus.Confirmed,
            database.transactionDao().getById(insertedId!!)?.status,
        )
    }

    @Test
    fun savePendingNotificationTransaction_fallsBackToDefaultAccount() = runTest {
        repository.ensureDefaultData()
        val defaultAccount = database.accountDao().listAll().first { it.type == AccountType.Alipay }
        repository.setDefaultAccount(defaultAccount.id)
        database.notificationRuleDao().upsert(
            NotificationRuleEntity(
                packageName = "custom.pay",
                appName = "CustomPay",
                enabled = true,
                keywords = listOf("消费"),
                amountPattern = "",
                directionPattern = "",
            ),
        )

        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-default-account").copy(
                sourceApp = "CustomPay",
                packageName = "custom.pay",
                rawTitle = "CustomPay",
                rawText = "消费12.00元",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(AccountType.Alipay, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesBankAccountByPartialSourceName() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-bank-account").copy(
                sourceApp = "银行",
                packageName = "com.example.bank",
                rawTitle = "交易提醒",
                rawText = "支出人民币12.00元",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_trimsParsedMerchant() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-trim-notification-merchant").copy(
                merchant = "  便利店  ",
            ),
        )

        assertTrue(insertedId != null)
        assertEquals("便利店", database.transactionDao().getById(insertedId!!)?.merchant)
    }

    @Test
    fun savePendingNotificationTransaction_storesBlankParsedMerchantAsNull() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-blank-notification-merchant").copy(
                merchant = "   ",
            ),
        )

        assertTrue(insertedId != null)
        assertNull(database.transactionDao().getById(insertedId!!)?.merchant)
    }

    @Test
    fun savePendingNotificationTransaction_matchesBankRuleBySourceName() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-bank-source-rule").copy(
                sourceApp = "银行",
                packageName = "com.cmbchina.ccd",
                rawTitle = "交易提醒",
                rawText = "支出人民币12.00元",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesBankRuleByPaymentKeyword() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-bank-payment-keyword").copy(
                sourceApp = "银行",
                packageName = "com.cmbchina.ccd",
                rawTitle = "动账提醒",
                rawText = "尾号1234付款人民币12.00元，商户：便利店",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesBankRuleByDebitKeyword() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-bank-debit-keyword").copy(
                sourceApp = "银行",
                packageName = "com.cmbchina.ccd",
                rawTitle = "动账提醒",
                rawText = "尾号1234扣款人民币12.00元，商户：便利店",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesBankRuleByTransferInKeyword() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-bank-transfer-in-keyword").copy(
                type = TransactionType.Income,
                sourceApp = "银行",
                packageName = "com.cmbchina.ccd",
                rawTitle = "动账提醒",
                rawText = "尾号1234转入人民币12.00元，付款方：公司",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(TransactionType.Income, saved?.type)
        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesBankRuleByDepositKeyword() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-bank-deposit-keyword").copy(
                type = TransactionType.Income,
                sourceApp = "银行",
                packageName = "com.cmbchina.ccd",
                rawTitle = "动账提醒",
                rawText = "尾号1234入账人民币12.00元，付款方：公司",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(TransactionType.Income, saved?.type)
        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesBankRuleBySalaryKeyword() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-bank-salary-keyword").copy(
                type = TransactionType.Income,
                sourceApp = "银行",
                packageName = "com.cmbchina.ccd",
                rawTitle = "动账提醒",
                rawText = "尾号1234工资发放人民币5000.00元，付款方：公司",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(TransactionType.Income, saved?.type)
        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesBankRuleByCreditKeyword() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-bank-credit-keyword").copy(
                type = TransactionType.Income,
                sourceApp = "银行",
                packageName = "com.cmbchina.ccd",
                rawTitle = "动账提醒",
                rawText = "尾号1234贷记人民币5000.00元，付款人：公司",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(TransactionType.Income, saved?.type)
        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesBankRuleByDebitAccountingKeyword() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-bank-debit-accounting-keyword").copy(
                sourceApp = "银行",
                packageName = "com.cmbchina.ccd",
                rawTitle = "动账提醒",
                rawText = "尾号1234借记人民币12.00元，收款方：便利店",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(TransactionType.Expense, saved?.type)
        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesAlipayRuleBySourceName() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-alipay-source-rule").copy(
                sourceApp = "支付宝",
                packageName = "com.Alipay.mobile",
                rawTitle = "支付宝",
                rawText = "支付12.00元",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(AccountType.Alipay, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesUnionPayAccountByPackage() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-unionpay-account").copy(
                sourceApp = "银联",
                packageName = "com.unionpay.wallet",
                rawTitle = "银联交易提醒",
                rawText = "消费45.60元，商户：超市",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val account = saved?.accountId?.let { database.accountDao().getById(it) }

        assertEquals(AccountType.Bank, account?.type)
    }

    @Test
    fun savePendingNotificationTransaction_matchesRefundIncomeCategory() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-refund-category").copy(
                type = TransactionType.Income,
                sourceApp = "支付宝",
                packageName = "com.eg.android.AlipayGphone",
                rawTitle = "支付宝",
                rawText = "退款到账8.00元 商户：咖啡店",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val category = saved?.categoryId?.let { database.categoryDao().getById(it) }

        assertEquals("退款", category?.name)
    }

    @Test
    fun savePendingNotificationTransaction_matchesTransferExpenseCategory() = runTest {
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-transfer-category").copy(
                sourceApp = "微信",
                packageName = "com.tencent.mm",
                rawTitle = "微信支付",
                rawText = "向朋友转账12.00元",
            ),
        )

        assertTrue(insertedId != null)
        val saved = database.transactionDao().getById(insertedId!!)
        val category = saved?.categoryId?.let { database.categoryDao().getById(it) }

        assertEquals("转账", category?.name)
    }

    @Test
    fun setNotificationRuleEnabled_updatesRuleState() = runTest {
        repository.ensureDefaultData()
        val rule = database.notificationRuleDao().listAll().first { it.packageName == "com.tencent.mm" }

        repository.setNotificationRuleEnabled(rule.id, false)

        assertEquals(false, database.notificationRuleDao().getById(rule.id)?.enabled)
    }

    @Test
    fun setDefaultAccount_ignoresMissingAccountId() = runTest {
        repository.ensureDefaultData()
        val defaultAccount = database.accountDao().listAll().first { it.type == AccountType.Alipay }
        repository.setDefaultAccount(defaultAccount.id)

        repository.setDefaultAccount(404L)

        assertEquals(defaultAccount.id, database.appSettingDao().get()?.defaultAccountId)
    }

    @Test
    fun exportSnapshot_doesNotUpdateLastExportedAtBeforeFileIsSaved() = runTest {
        repository.ensureDefaultData()

        val snapshot = repository.exportSnapshot()
        val savedSettings = database.appSettingDao().get()

        assertTrue(snapshot.exportedAt > 0L)
        assertEquals(null, snapshot.settings?.lastExportedAt)
        assertEquals(null, savedSettings?.lastExportedAt)
    }

    @Test
    fun markExportCompleted_updatesLastExportedAt() = runTest {
        repository.ensureDefaultData()
        val exportedAt = 2_000L

        repository.markExportCompleted(exportedAt)

        val savedSettings = database.appSettingDao().get()
        assertEquals(exportedAt, savedSettings?.lastExportedAt)
        assertEquals(exportedAt, savedSettings?.updatedAt)
    }

    @Test
    fun exportSnapshot_listsActiveTransactionsInLedgerOrder() = runTest {
        repository.ensureDefaultData()
        val categoryId = database.categoryDao().listByType(TransactionType.Expense).first().id
        val accountId = database.accountDao().listAll().first().id
        val oldestId = repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 100L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = 1_000L,
            ),
        )
        val newestId = repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 200L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = 3_000L,
            ),
        )
        val deletedId = repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 300L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = 2_000L,
            ),
        )

        repository.softDeleteTransaction(deletedId)

        val snapshot = repository.exportSnapshot()

        assertEquals(listOf(newestId, oldestId), snapshot.transactions.map { it.id })
    }

    @Test
    fun exportSnapshot_includesPendingTransactionsForBackup() = runTest {
        val pendingId = repository.savePendingNotificationTransaction(sampleParsed("fingerprint-export-pending"))

        assertTrue(pendingId != null)

        val snapshot = repository.exportSnapshot()

        assertEquals(listOf(pendingId), snapshot.transactions.map { it.id })
        assertEquals(
            TransactionStatus.Pending,
            snapshot.transactions.first().status,
        )
    }

    @Test
    fun saveManualTransaction_rejectsNonPositiveAmount() = runTest {
        repository.ensureDefaultData()
        val categoryId = database.categoryDao().listByType(TransactionType.Expense).first().id
        val accountId = database.accountDao().listAll().first().id

        try {
            repository.saveManualTransaction(
                sampleTransaction(
                    amountCents = -1L,
                    categoryId = categoryId,
                    accountId = accountId,
                    occurredAt = 1_000L,
                ),
            )
            fail("Expected IllegalArgumentException for non-positive amount.")
        } catch (_: IllegalArgumentException) {
        }
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun saveManualTransaction_trimsMerchantAndNote() = runTest {
        repository.ensureDefaultData()
        val categoryId = database.categoryDao().listByType(TransactionType.Expense).first().id
        val accountId = database.accountDao().listAll().first().id

        val insertedId = repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 100L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = 1_000L,
            ).copy(
                merchant = "  便利店  ",
                note = "  早餐  ",
            ),
        )

        val saved = database.transactionDao().getById(insertedId)
        assertEquals("便利店", saved?.merchant)
        assertEquals("早餐", saved?.note)
    }

    @Test
    fun saveManualTransaction_updatesExistingTransactionInPlace() = runTest {
        repository.ensureDefaultData()
        val categoryId = database.categoryDao().listByType(TransactionType.Expense).first().id
        val accountId = database.accountDao().listAll().first().id
        val insertedId = repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 100L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = 1_000L,
            ),
        )
        val original = database.transactionDao().getById(insertedId)

        Thread.sleep(2)
        val updatedId = repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 250L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = 2_000L,
            ).copy(
                id = insertedId,
                merchant = "咖啡店",
                note = "下午茶",
            ),
        )

        val updated = database.transactionDao().getById(insertedId)
        assertEquals(insertedId, updatedId)
        assertEquals(250L, updated?.amountCents)
        assertEquals("咖啡店", updated?.merchant)
        assertEquals("下午茶", updated?.note)
        assertEquals(2_000L, updated?.occurredAt)
        assertEquals(original?.createdAt, updated?.createdAt)
        assertEquals(original?.source, updated?.source)
        assertTrue((updated?.updatedAt ?: 0L) >= (original?.updatedAt ?: 0L))
    }

    @Test
    fun saveManualTransaction_updatesPendingNotificationWithoutLosingNotificationMetadata() = runTest {
        val fingerprint = "fingerprint-edit-pending-notification"
        val insertedId = repository.savePendingNotificationTransaction(sampleParsed(fingerprint))

        assertTrue(insertedId != null)
        val original = database.transactionDao().getById(insertedId!!)

        Thread.sleep(2)
        repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 3400L,
                categoryId = original!!.categoryId,
                accountId = original.accountId,
                occurredAt = 2_000L,
            ).copy(
                id = insertedId,
                merchant = "编辑后的商户",
                note = "确认前补充",
                status = TransactionStatus.Pending,
            ),
        )

        val updated = database.transactionDao().getById(insertedId)
        assertEquals(TransactionSource.Notification, updated?.source)
        assertEquals(fingerprint, updated?.rawNotificationId)
        assertEquals(TransactionStatus.Pending, updated?.status)
        assertEquals(3400L, updated?.amountCents)
        assertEquals("编辑后的商户", updated?.merchant)
        assertEquals("确认前补充", updated?.note)
        assertEquals(original.createdAt, updated?.createdAt)
        assertTrue((updated?.updatedAt ?: 0L) >= original.updatedAt)
    }

    @Test
    fun saveManualTransaction_rejectsMissingReferences() = runTest {
        repository.ensureDefaultData()
        val categoryId = database.categoryDao().listByType(TransactionType.Expense).first().id
        val accountId = database.accountDao().listAll().first().id

        try {
            repository.saveManualTransaction(
                sampleTransaction(
                    amountCents = 100L,
                    categoryId = 404L,
                    accountId = accountId,
                    occurredAt = 1_000L,
                ),
            )
            fail("Expected IllegalArgumentException for missing category.")
        } catch (_: IllegalArgumentException) {
        }
        try {
            repository.saveManualTransaction(
                sampleTransaction(
                    amountCents = 100L,
                    categoryId = categoryId,
                    accountId = 404L,
                    occurredAt = 1_000L,
                ),
            )
            fail("Expected IllegalArgumentException for missing account.")
        } catch (_: IllegalArgumentException) {
        }

        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun saveManualTransaction_rejectsCategoryTypeMismatch() = runTest {
        repository.ensureDefaultData()
        val incomeCategoryId = database.categoryDao().listByType(TransactionType.Income).first().id
        val accountId = database.accountDao().listAll().first().id

        try {
            repository.saveManualTransaction(
                sampleTransaction(
                    amountCents = 100L,
                    categoryId = incomeCategoryId,
                    accountId = accountId,
                    occurredAt = 1_000L,
                ),
            )
            fail("Expected IllegalArgumentException for category type mismatch.")
        } catch (_: IllegalArgumentException) {
        }

        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun saveManualTransaction_rejectsMissingUpdateTarget() = runTest {
        repository.ensureDefaultData()
        val categoryId = database.categoryDao().listByType(TransactionType.Expense).first().id
        val accountId = database.accountDao().listAll().first().id

        try {
            repository.saveManualTransaction(
                sampleTransaction(
                    amountCents = 100L,
                    categoryId = categoryId,
                    accountId = accountId,
                    occurredAt = 1_000L,
                ).copy(id = 404L),
            )
            fail("Expected IllegalArgumentException for missing update target.")
        } catch (_: IllegalArgumentException) {
        }
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun saveManualTransaction_rejectsDeletedUpdateTarget() = runTest {
        repository.ensureDefaultData()
        val categoryId = database.categoryDao().listByType(TransactionType.Expense).first().id
        val accountId = database.accountDao().listAll().first().id
        val id = repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 100L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = 1_000L,
            ),
        )

        repository.softDeleteTransaction(id)
        try {
            repository.saveManualTransaction(
                sampleTransaction(
                    amountCents = 200L,
                    categoryId = categoryId,
                    accountId = accountId,
                    occurredAt = 2_000L,
                ).copy(id = id),
            )
            fail("Expected IllegalArgumentException for deleted update target.")
        } catch (_: IllegalArgumentException) {
        }

        assertEquals(TransactionStatus.Deleted, database.transactionDao().getById(id)?.status)
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun observeStats_includesDailyExpensesForLastSevenDays() = runTest {
        val zone = ZoneId.of("Asia/Shanghai")
        val today = LocalDate.now(zone)
        repository.ensureDefaultData()
        val categoryId = database.categoryDao().listByType(TransactionType.Expense).first().id
        val accountId = database.accountDao().listAll().first().id

        repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 100L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = today.atStartOfDay(zone).toInstant().toEpochMilli(),
            ),
        )
        repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 200L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli(),
            ),
        )
        repository.saveManualTransaction(
            sampleTransaction(
                amountCents = 300L,
                categoryId = categoryId,
                accountId = accountId,
                occurredAt = today.minusDays(7).atStartOfDay(zone).toInstant().toEpochMilli(),
            ),
        )

        val stats = repository.observeStats(zone, YearMonth.now(zone)).first()

        assertEquals(300L, stats.last7DaysExpenseCents)
        assertEquals(7, stats.dailyExpenses.size)
        assertEquals(200L, stats.dailyExpenses.first().amountCents)
        assertEquals(100L, stats.dailyExpenses.last().amountCents)
    }

    @Test
    fun observeSummaryAndStats_hidePendingUntilConfirmed() = runTest {
        val zone = ZoneId.of("Asia/Shanghai")
        val month = YearMonth.of(2026, 7)
        val postedAt = LocalDate.of(2026, 7, 8).atStartOfDay(zone).toInstant().toEpochMilli()
        val insertedId = repository.savePendingNotificationTransaction(
            sampleParsed("fingerprint-stats-pending").copy(postedAt = postedAt),
        )

        assertTrue(insertedId != null)
        assertEquals(0L, repository.observeMonthSummary(zone, month).first().expenseCents)
        assertEquals(0L, repository.observeStats(zone, month).first().summary.expenseCents)

        repository.confirmPendingTransaction(insertedId!!)

        assertEquals(1200L, repository.observeMonthSummary(zone, month).first().expenseCents)
        assertEquals(1200L, repository.observeStats(zone, month).first().summary.expenseCents)
    }

    private fun sampleParsed(fingerprint: String): PaymentNotificationParseResult {
        return PaymentNotificationParseResult(
            amountCents = 1200L,
            type = TransactionType.Expense,
            merchant = "便利店",
            sourceApp = "微信",
            fingerprint = fingerprint,
            postedAt = 1000L,
            rawTitle = "微信支付",
            rawText = "向便利店付款12.00元",
            packageName = "com.tencent.mm",
        )
    }

    private fun sampleTransaction(
        amountCents: Long,
        categoryId: Long,
        accountId: Long,
        occurredAt: Long,
    ): SaveTransactionInput {
        return SaveTransactionInput(
            type = TransactionType.Expense,
            amountCents = amountCents,
            categoryId = categoryId,
            accountId = accountId,
            merchant = "",
            note = "",
            occurredAt = occurredAt,
        )
    }
}
