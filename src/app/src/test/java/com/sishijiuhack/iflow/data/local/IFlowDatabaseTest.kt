package com.sishijiuhack.iflow.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sishijiuhack.iflow.data.local.entity.NotificationRuleEntity
import com.sishijiuhack.iflow.data.local.entity.TransactionEntity
import com.sishijiuhack.iflow.domain.model.TransactionSource
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IFlowDatabaseTest {
    private lateinit var database: IFlowDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IFlowDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedIfNeeded_insertsDefaultCategoriesAccountsAndSettings() = runTest {
        DefaultDataSeeder(database).seedIfNeeded(nowMillis = 1000L)

        assertEquals(DefaultLedgerData.categories.size, database.categoryDao().count())
        assertEquals(DefaultLedgerData.accounts.size, database.accountDao().count())
        assertEquals(DefaultLedgerData.notificationRules.size, database.notificationRuleDao().count())
        assertTrue(database.notificationRuleDao().listEnabled().any { it.appName == "微信" })

        val setting = database.appSettingDao().get()
        assertNotNull(setting)
        assertEquals(DefaultLedgerData.DefaultAccountId, setting?.defaultAccountId)
        assertEquals(false, setting?.autoConfirmEnabled)
    }

    @Test
    fun seedIfNeeded_insertsExpandedDefaultNotificationRuleMetadata() = runTest {
        DefaultDataSeeder(database).seedIfNeeded(nowMillis = 1000L)

        val rules = database.notificationRuleDao().listAll()

        assertTrue(rules.all { it.amountPattern.contains("RMB") })
        assertTrue(rules.all { it.amountPattern.contains("CNY") })
        assertTrue(rules.all { it.amountPattern.contains("(?i:RMB|CNY)") })
        assertTrue(rules.all { it.amountPattern.contains("[:：]?") })
        assertTrue(rules.all { it.amountPattern.contains("[,，]") })
        assertTrue(rules.all { it.amountPattern.contains("０-９") })
        assertTrue(rules.all { it.amountPattern.contains("[.．。]") })
        assertTrue(rules.all { it.amountPattern.contains("块") })
        assertTrue(rules.all { it.directionPattern.contains("扣款") })
        assertTrue(rules.all { it.directionPattern.contains("入账") })
        assertTrue(rules.all { it.directionPattern.contains("存入") })
        assertTrue(rules.all { it.directionPattern.contains("工资") })
        assertTrue(rules.all { it.directionPattern.contains("贷记") })
        assertTrue(rules.all { it.directionPattern.contains("借记") })
        assertTrue(rules.all { it.merchantPattern?.contains("商户名称") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("交易商户") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("交易方") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("对手户名") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("对方户名") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("收款户名") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("收款账户") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("收款人") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("付款账户") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("付款户名") == true })
        assertTrue(rules.all { it.merchantPattern?.contains("付款人") == true })
    }

    @Test
    fun transactionDao_insertsListsUpdatesAndSoftDeletesTransactions() = runTest {
        DefaultDataSeeder(database).seedIfNeeded(nowMillis = 1000L)
        val now = 2000L

        val insertedId = database.transactionDao().insert(
            TransactionEntity(
                type = TransactionType.Expense,
                amountCents = 1299L,
                categoryId = 1L,
                accountId = DefaultLedgerData.DefaultAccountId,
                merchant = "便利店",
                note = "早餐",
                occurredAt = now,
                source = TransactionSource.Manual,
                status = TransactionStatus.Confirmed,
                rawNotificationId = null,
                createdAt = now,
                updatedAt = now,
            ),
        )

        val inserted = database.transactionDao().getById(insertedId)
        assertNotNull(inserted)
        assertEquals(1299L, inserted?.amountCents)

        val updated = inserted!!.copy(note = "早餐和咖啡", updatedAt = now + 1)
        database.transactionDao().update(updated)
        assertEquals("早餐和咖啡", database.transactionDao().getById(insertedId)?.note)

        assertEquals(1, database.transactionDao().listActiveTransactions().size)
        database.transactionDao().updateStatus(insertedId, TransactionStatus.Deleted, now + 2)
        assertTrue(database.transactionDao().listActiveTransactions().isEmpty())
    }

    @Test
    fun transactionDao_listsActiveTransactionsInStableLedgerOrder() = runTest {
        DefaultDataSeeder(database).seedIfNeeded(nowMillis = 1000L)
        val olderId = database.transactionDao().insert(sampleTransaction(occurredAt = 1_000L))
        val sameTimeOlderId = database.transactionDao().insert(sampleTransaction(occurredAt = 2_000L))
        val sameTimeNewerId = database.transactionDao().insert(sampleTransaction(occurredAt = 2_000L))

        val activeIds = database.transactionDao().listActiveTransactions().map { it.id }

        assertEquals(listOf(sameTimeNewerId, sameTimeOlderId, olderId), activeIds)
    }

    @Test
    fun notificationRuleDao_preservesKeywordListWithConverters() = runTest {
        val id = database.notificationRuleDao().upsert(
            NotificationRuleEntity(
                packageName = "com.tencent.mm",
                appName = "微信",
                enabled = true,
                keywords = listOf("微信支付", "付款"),
                amountPattern = """¥?(\d+(?:\.\d{1,2})?)""",
                directionPattern = "付款|收款|退款",
                merchantPattern = null,
            ),
        )

        val rule = database.notificationRuleDao().getById(id)
        assertEquals(listOf("微信支付", "付款"), rule?.keywords)
    }

    private fun sampleTransaction(occurredAt: Long): TransactionEntity {
        return TransactionEntity(
            type = TransactionType.Expense,
            amountCents = 1299L,
            categoryId = 1L,
            accountId = DefaultLedgerData.DefaultAccountId,
            merchant = "便利店",
            note = "早餐",
            occurredAt = occurredAt,
            source = TransactionSource.Manual,
            status = TransactionStatus.Confirmed,
            rawNotificationId = null,
            createdAt = occurredAt,
            updatedAt = occurredAt,
        )
    }
}
