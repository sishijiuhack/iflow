package com.sishijiuhack.iflow.data.repository

import com.sishijiuhack.iflow.data.local.DefaultDataSeeder
import com.sishijiuhack.iflow.data.local.IFlowDatabase
import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.local.entity.TransactionEntity
import com.sishijiuhack.iflow.domain.model.TransactionSource
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType
import com.sishijiuhack.iflow.notification.PaymentNotificationParseResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.time.ZoneId

class LedgerRepository(
    private val database: IFlowDatabase,
    private val seeder: DefaultDataSeeder,
) {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val accountDao = database.accountDao()

    suspend fun ensureDefaultData() {
        seeder.seedIfNeeded()
    }

    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.observeAll()

    fun observeTransactions(): Flow<List<TransactionListItem>> {
        return combine(
            transactionDao.observeActiveTransactions(),
            categoryDao.observeAll(),
            accountDao.observeAll(),
        ) { transactions, categories, accounts ->
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            transactions.map {
                it.toListItem(
                    categoryName = categoryMap[it.categoryId]?.name ?: "未分类",
                    accountName = accountMap[it.accountId]?.name ?: "未知账户",
                )
            }
        }
    }

    fun observeRecentTransactions(limit: Int): Flow<List<TransactionListItem>> {
        return combine(
            transactionDao.observeRecentActiveTransactions(limit),
            categoryDao.observeAll(),
            accountDao.observeAll(),
        ) { transactions, categories, accounts ->
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            transactions.map {
                it.toListItem(
                    categoryName = categoryMap[it.categoryId]?.name ?: "未分类",
                    accountName = accountMap[it.accountId]?.name ?: "未知账户",
                )
            }
        }
    }

    fun observePendingCount(): Flow<Int> {
        return transactionDao.observeCountByStatus(TransactionStatus.Pending)
    }

    fun observePendingTransactions(): Flow<List<TransactionListItem>> {
        return combine(
            transactionDao.observeByStatus(TransactionStatus.Pending),
            categoryDao.observeAll(),
            accountDao.observeAll(),
        ) { transactions, categories, accounts ->
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            transactions.map {
                it.toListItem(
                    categoryName = categoryMap[it.categoryId]?.name ?: "未分类",
                    accountName = accountMap[it.accountId]?.name ?: "未知账户",
                )
            }
        }
    }

    fun observeMonthSummary(
        zoneId: ZoneId = ZoneId.systemDefault(),
        month: YearMonth = YearMonth.now(zoneId),
    ): Flow<MonthSummary> {
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return transactionDao.observeActiveTransactions().map { transactions ->
            val confirmed = transactions.filter {
                it.status == TransactionStatus.Confirmed && it.occurredAt in start until end
            }
            val income = confirmed
                .filter { it.type == TransactionType.Income }
                .sumOf { it.amountCents }
            val expense = confirmed
                .filter { it.type == TransactionType.Expense }
                .sumOf { it.amountCents }
            MonthSummary(
                incomeCents = income,
                expenseCents = expense,
                balanceCents = income - expense,
            )
        }
    }

    fun observeStats(
        zoneId: ZoneId = ZoneId.systemDefault(),
        month: YearMonth = YearMonth.now(zoneId),
    ): Flow<StatsSnapshot> {
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return combine(
            transactionDao.observeActiveTransactions(),
            categoryDao.observeAll(),
        ) { transactions, categories ->
            val categoryMap = categories.associateBy { it.id }
            val confirmed = transactions.filter {
                it.status == TransactionStatus.Confirmed && it.occurredAt in start until end
            }
            val income = confirmed
                .filter { it.type == TransactionType.Income }
                .sumOf { it.amountCents }
            val expense = confirmed
                .filter { it.type == TransactionType.Expense }
                .sumOf { it.amountCents }
            val ranking = confirmed
                .filter { it.type == TransactionType.Expense }
                .groupBy { it.categoryId }
                .map { (categoryId, rows) ->
                    CategoryExpense(
                        categoryName = categoryMap[categoryId]?.name ?: "未分类",
                        amountCents = rows.sumOf { it.amountCents },
                    )
                }
                .sortedByDescending { it.amountCents }

            StatsSnapshot(
                summary = MonthSummary(
                    incomeCents = income,
                    expenseCents = expense,
                    balanceCents = income - expense,
                ),
                categoryExpenses = ranking,
            )
        }
    }

    suspend fun getTransaction(id: Long): TransactionEntity? = transactionDao.getById(id)

    suspend fun getCategory(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun getAccount(id: Long): AccountEntity? = accountDao.getById(id)

    suspend fun exportSnapshot(): LedgerExportSnapshot {
        ensureDefaultData()
        return LedgerExportSnapshot(
            exportedAt = System.currentTimeMillis(),
            transactions = transactionDao.listActiveTransactions(),
            categories = categoryDao.observeAllSnapshot(),
            accounts = accountDao.listAll(),
            settings = database.appSettingDao().get(),
        )
    }

    suspend fun saveManualTransaction(input: SaveTransactionInput): Long {
        ensureDefaultData()
        val now = System.currentTimeMillis()
        val existing = input.id?.let { transactionDao.getById(it) }
        val entity = TransactionEntity(
            id = input.id ?: 0L,
            type = input.type,
            amountCents = input.amountCents,
            categoryId = input.categoryId,
            accountId = input.accountId,
            merchant = input.merchant.trim().ifBlank { null },
            note = input.note.trim().ifBlank { null },
            occurredAt = input.occurredAt,
            source = existing?.source ?: TransactionSource.Manual,
            status = input.status,
            rawNotificationId = existing?.rawNotificationId,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )

        return if (input.id == null) {
            transactionDao.insert(entity)
        } else {
            transactionDao.update(entity)
            input.id
        }
    }

    suspend fun savePendingNotificationTransaction(parsed: PaymentNotificationParseResult): Long? {
        ensureDefaultData()
        if (transactionDao.countByRawNotificationId(parsed.fingerprint) > 0) return null
        val categories = categoryDao.listByType(parsed.type)
        val accounts = accountDao.listAll()
        val now = System.currentTimeMillis()
        val categoryId = categories.firstOrNull()?.id ?: return null
        val accountId = accounts.firstOrNull { parsed.sourceApp.contains(it.name) }?.id
            ?: accounts.firstOrNull()?.id
            ?: return null

        return transactionDao.insert(
            TransactionEntity(
                type = parsed.type,
                amountCents = parsed.amountCents,
                categoryId = categoryId,
                accountId = accountId,
                merchant = parsed.merchant,
                note = "${parsed.sourceApp}通知",
                occurredAt = parsed.postedAt,
                source = TransactionSource.Notification,
                status = TransactionStatus.Pending,
                rawNotificationId = parsed.fingerprint,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun softDeleteTransaction(id: Long) {
        transactionDao.updateStatus(id, TransactionStatus.Deleted, System.currentTimeMillis())
    }

    suspend fun confirmPendingTransaction(id: Long) {
        transactionDao.updateStatus(id, TransactionStatus.Confirmed, System.currentTimeMillis())
    }

    private fun TransactionEntity.toListItem(
        categoryName: String,
        accountName: String,
    ): TransactionListItem {
        return TransactionListItem(
            id = id,
            type = type,
            amountCents = amountCents,
            categoryName = categoryName,
            accountName = accountName,
            merchant = merchant,
            note = note,
            occurredAt = occurredAt,
            source = source,
            status = status,
        )
    }
}

data class SaveTransactionInput(
    val id: Long? = null,
    val type: TransactionType,
    val amountCents: Long,
    val categoryId: Long,
    val accountId: Long,
    val merchant: String,
    val note: String,
    val occurredAt: Long,
    val status: TransactionStatus = TransactionStatus.Confirmed,
)

data class TransactionListItem(
    val id: Long,
    val type: TransactionType,
    val amountCents: Long,
    val categoryName: String,
    val accountName: String,
    val merchant: String?,
    val note: String?,
    val occurredAt: Long,
    val source: TransactionSource,
    val status: TransactionStatus,
)

data class MonthSummary(
    val incomeCents: Long = 0,
    val expenseCents: Long = 0,
    val balanceCents: Long = 0,
)

data class StatsSnapshot(
    val summary: MonthSummary = MonthSummary(),
    val categoryExpenses: List<CategoryExpense> = emptyList(),
)

data class CategoryExpense(
    val categoryName: String,
    val amountCents: Long,
)

data class LedgerExportSnapshot(
    val exportedAt: Long,
    val transactions: List<TransactionEntity>,
    val categories: List<CategoryEntity>,
    val accounts: List<AccountEntity>,
    val settings: com.sishijiuhack.iflow.data.local.entity.AppSettingEntity?,
)
