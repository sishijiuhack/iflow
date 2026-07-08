package com.sishijiuhack.iflow.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sishijiuhack.iflow.data.local.DefaultDataSeeder
import com.sishijiuhack.iflow.data.local.IFlowDatabase
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType
import com.sishijiuhack.iflow.notification.PaymentNotificationParseResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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
    fun setNotificationRuleEnabled_updatesRuleState() = runTest {
        repository.ensureDefaultData()
        val rule = database.notificationRuleDao().listAll().first { it.packageName == "com.tencent.mm" }

        repository.setNotificationRuleEnabled(rule.id, false)

        assertEquals(false, database.notificationRuleDao().getById(rule.id)?.enabled)
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
}
