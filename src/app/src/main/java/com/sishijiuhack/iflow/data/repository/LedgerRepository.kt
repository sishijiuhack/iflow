package com.sishijiuhack.iflow.data.repository

import com.sishijiuhack.iflow.data.local.DefaultDataSeeder
import com.sishijiuhack.iflow.data.local.IFlowDatabase
import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.AppSettingEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.local.entity.NotificationRuleEntity
import com.sishijiuhack.iflow.data.local.entity.TransactionEntity
import com.sishijiuhack.iflow.domain.model.AccountType
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
    private val notificationRuleDao = database.notificationRuleDao()
    private val appSettingDao = database.appSettingDao()

    suspend fun ensureDefaultData() {
        seeder.seedIfNeeded()
    }

    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.observeAll()

    fun observeNotificationRules(): Flow<List<NotificationRuleEntity>> = notificationRuleDao.observeAll()

    fun observeSettings(): Flow<AppSettingEntity?> = appSettingDao.observe()

    fun observeTransactions(): Flow<List<TransactionListItem>> {
        return combine(
            transactionDao.observeByStatus(TransactionStatus.Confirmed),
            categoryDao.observeAll(),
            accountDao.observeAll(),
        ) { transactions, categories, accounts ->
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            transactions.map {
                val category = categoryMap[it.categoryId]
                it.toListItem(
                    categoryName = category?.name ?: "未分类",
                    categoryIcon = category?.icon,
                    accountName = accountMap[it.accountId]?.name ?: "未知账户",
                )
            }
        }
    }

    fun observeRecentTransactions(limit: Int): Flow<List<TransactionListItem>> {
        return combine(
            transactionDao.observeRecentByStatus(TransactionStatus.Confirmed, limit),
            categoryDao.observeAll(),
            accountDao.observeAll(),
        ) { transactions, categories, accounts ->
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            transactions.map {
                val category = categoryMap[it.categoryId]
                it.toListItem(
                    categoryName = category?.name ?: "未分类",
                    categoryIcon = category?.icon,
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
                val category = categoryMap[it.categoryId]
                it.toListItem(
                    categoryName = category?.name ?: "未分类",
                    categoryIcon = category?.icon,
                    accountName = accountMap[it.accountId]?.name ?: "未知账户",
                )
            }
        }
    }

    fun observeAccountBalances(): Flow<AccountBalanceSummary> {
        return combine(
            transactionDao.observeByStatus(TransactionStatus.Confirmed),
            accountDao.observeAll(),
        ) { transactions, accounts ->
            val items = accounts.map { account ->
                val balance = transactions
                    .filter { it.accountId == account.id }
                    .sumOf { transaction ->
                        when (transaction.type) {
                            TransactionType.Income -> transaction.amountCents
                            TransactionType.Expense -> -transaction.amountCents
                        }
                    }
                AccountBalanceItem(
                    accountId = account.id,
                    accountName = account.name,
                    accountType = account.type,
                    balanceCents = balance,
                )
            }
            val netAssets = items.sumOf { it.balanceCents }
            AccountBalanceSummary(
                netAssetsCents = netAssets,
                totalAssetsCents = items.filter { it.balanceCents > 0L }.sumOf { it.balanceCents },
                totalLiabilitiesCents = items.filter { it.balanceCents < 0L }.sumOf { -it.balanceCents },
                accounts = items,
            )
        }
    }

    fun observeMonthSummary(
        zoneId: ZoneId = ZoneId.systemDefault(),
        month: YearMonth = YearMonth.now(zoneId),
    ): Flow<MonthSummary> {
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return transactionDao.observeByStatus(TransactionStatus.Confirmed).map { transactions ->
            val monthConfirmed = transactions.filter {
                it.occurredAt in start until end
            }
            val income = monthConfirmed
                .filter { it.type == TransactionType.Income }
                .sumOf { it.amountCents }
            val expense = monthConfirmed
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
        val today = java.time.LocalDate.now(zoneId)
        val todayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val tomorrowStart = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val last7DaysStart = today.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return combine(
            transactionDao.observeByStatus(TransactionStatus.Confirmed),
            categoryDao.observeAll(),
        ) { transactions, categories ->
            val categoryMap = categories.associateBy { it.id }
            val monthConfirmed = transactions.filter {
                it.occurredAt in start until end
            }
            val income = monthConfirmed
                .filter { it.type == TransactionType.Income }
                .sumOf { it.amountCents }
            val expense = monthConfirmed
                .filter { it.type == TransactionType.Expense }
                .sumOf { it.amountCents }
            val todayExpense = transactions
                .filter {
                    it.type == TransactionType.Expense && it.occurredAt in todayStart until tomorrowStart
                }
                .sumOf { it.amountCents }
            val last7DaysExpense = transactions
                .filter {
                    it.type == TransactionType.Expense && it.occurredAt in last7DaysStart until tomorrowStart
                }
                .sumOf { it.amountCents }
            val dailyExpenses = (0..6).map { offset ->
                val day = today.minusDays((6 - offset).toLong())
                val dayStart = day.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val dayEnd = day.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                DailyExpense(
                    label = "${day.monthValue}/${day.dayOfMonth}",
                    amountCents = transactions
                        .filter {
                            it.type == TransactionType.Expense && it.occurredAt in dayStart until dayEnd
                        }
                        .sumOf { it.amountCents },
                )
            }
            val ranking = monthConfirmed
                .filter { it.type == TransactionType.Expense }
                .groupBy { it.categoryId }
                .map { (categoryId, rows) ->
                    CategoryExpense(
                        categoryName = categoryMap[categoryId]?.name ?: "未分类",
                        amountCents = rows.sumOf { it.amountCents },
                    )
                }
                .sortedWith(
                    compareByDescending<CategoryExpense> { it.amountCents }
                        .thenBy { it.categoryName },
                )

            StatsSnapshot(
                summary = MonthSummary(
                    incomeCents = income,
                    expenseCents = expense,
                    balanceCents = income - expense,
                ),
                todayExpenseCents = todayExpense,
                last7DaysExpenseCents = last7DaysExpense,
                dailyExpenses = dailyExpenses,
                categoryExpenses = ranking,
            )
        }
    }

    suspend fun getTransaction(id: Long): TransactionEntity? = transactionDao.getById(id)

    suspend fun getCategory(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun getAccount(id: Long): AccountEntity? = accountDao.getById(id)

    suspend fun exportSnapshot(): LedgerExportSnapshot {
        ensureDefaultData()
        val exportedAt = System.currentTimeMillis()
        return LedgerExportSnapshot(
            exportedAt = exportedAt,
            transactions = transactionDao.listActiveTransactions(),
            categories = categoryDao.observeAllSnapshot(),
            accounts = accountDao.listAll(),
            notificationRules = notificationRuleDao.listAll(),
            settings = appSettingDao.get(),
        )
    }

    suspend fun markExportCompleted(exportedAt: Long) {
        ensureDefaultData()
        val current = appSettingDao.get() ?: return
        appSettingDao.update(
            current.copy(
                lastExportedAt = exportedAt,
                updatedAt = exportedAt,
            ),
        )
    }

    suspend fun saveManualTransaction(input: SaveTransactionInput): Long {
        ensureDefaultData()
        require(input.amountCents > 0L) { "Transaction amount must be positive." }
        val now = System.currentTimeMillis()
        val existing = input.id?.let { transactionDao.getById(it) }
        require(input.id == null || existing != null) { "Transaction to update was not found." }
        require(existing?.status != TransactionStatus.Deleted) { "Deleted transactions cannot be edited." }
        val category = categoryDao.getById(input.categoryId)
        require(category != null) { "Transaction category was not found." }
        require(category.type == input.type) { "Transaction category type does not match the transaction type." }
        require(accountDao.getById(input.accountId) != null) { "Transaction account was not found." }
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
        if (parsed.amountCents <= 0L) return null
        val settings = appSettingDao.get()
        if (settings?.autoCaptureEnabled == false) return null
        if (!hasEnabledRuleFor(parsed)) return null
        val categories = categoryDao.listByType(parsed.type)
        val accounts = accountDao.listAll()
        val now = System.currentTimeMillis()
        val status = if (settings?.autoConfirmEnabled == true) {
            TransactionStatus.Confirmed
        } else {
            TransactionStatus.Pending
        }
        val categoryId = selectNotificationCategoryId(parsed, categories) ?: return null
        val accountId = selectNotificationAccountId(parsed, accounts, settings?.defaultAccountId)
            ?: accounts.firstOrNull()?.id
            ?: return null

        val insertedId = transactionDao.insertIgnoringConflict(
            TransactionEntity(
                type = parsed.type,
                amountCents = parsed.amountCents,
                categoryId = categoryId,
                accountId = accountId,
                merchant = parsed.merchant?.trim()?.ifBlank { null },
                note = "${parsed.sourceApp}通知",
                occurredAt = parsed.postedAt,
                source = TransactionSource.Notification,
                status = status,
                rawNotificationId = parsed.fingerprint,
                createdAt = now,
                updatedAt = now,
            ),
        )
        return insertedId.takeIf { it != -1L }
    }

    suspend fun softDeleteTransaction(id: Long) {
        val transaction = transactionDao.getById(id) ?: return
        if (transaction.status == TransactionStatus.Deleted) return
        transactionDao.updateStatus(id, TransactionStatus.Deleted, System.currentTimeMillis())
    }

    suspend fun confirmPendingTransaction(id: Long) {
        val transaction = transactionDao.getById(id) ?: return
        if (transaction.status != TransactionStatus.Pending) return
        transactionDao.updateStatus(id, TransactionStatus.Confirmed, System.currentTimeMillis())
    }

    suspend fun setAutoCaptureEnabled(enabled: Boolean) {
        ensureDefaultData()
        val current = appSettingDao.get() ?: return
        appSettingDao.update(
            current.copy(
                autoCaptureEnabled = enabled,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun setAutoConfirmEnabled(enabled: Boolean) {
        ensureDefaultData()
        val current = appSettingDao.get() ?: return
        appSettingDao.update(
            current.copy(
                autoConfirmEnabled = enabled,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun setDefaultAccount(accountId: Long) {
        ensureDefaultData()
        if (accountDao.getById(accountId) == null) return
        val current = appSettingDao.get() ?: return
        appSettingDao.update(
            current.copy(
                defaultAccountId = accountId,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun setNotificationRuleEnabled(ruleId: Long, enabled: Boolean) {
        ensureDefaultData()
        val rule = notificationRuleDao.getById(ruleId) ?: return
        notificationRuleDao.update(rule.copy(enabled = enabled))
    }

    private suspend fun hasEnabledRuleFor(parsed: PaymentNotificationParseResult): Boolean {
        val text = listOf(parsed.rawTitle, parsed.rawText).joinToString(" ")
        return notificationRuleDao.listEnabled().any { rule ->
            val packageMatches = parsed.packageName == rule.packageName ||
                parsed.packageName.contains(rule.packageName, ignoreCase = true) ||
                (rule.packageName == "bank" && parsed.sourceApp.contains("银行")) ||
                (rule.packageName == "com.eg.android.AlipayGphone" && parsed.sourceApp.contains("支付宝"))
            val keywordMatches = rule.keywords.isEmpty() ||
                rule.keywords.any { keyword -> text.contains(keyword, ignoreCase = true) }
            packageMatches && keywordMatches
        }
    }

    private fun selectNotificationAccountId(
        parsed: PaymentNotificationParseResult,
        accounts: List<AccountEntity>,
        defaultAccountId: Long?,
    ): Long? {
        return accounts.firstOrNull {
            parsed.sourceApp.contains(it.name) || it.name.contains(parsed.sourceApp)
        }?.id ?: preferredAccountType(parsed)?.let { accountType ->
            accounts.firstOrNull { it.type == accountType }?.id
        } ?: defaultAccountId?.let { id ->
            accounts.firstOrNull { it.id == id }?.id
        }
    }

    private fun preferredAccountType(parsed: PaymentNotificationParseResult): AccountType? {
        val packageName = parsed.packageName
        return when {
            packageName == "com.tencent.mm" -> AccountType.Wechat
            packageName.contains("alipay", ignoreCase = true) -> AccountType.Alipay
            packageName.contains("unionpay", ignoreCase = true) -> AccountType.Bank
            packageName.contains("bank", ignoreCase = true) -> AccountType.Bank
            else -> null
        }
    }

    private fun selectNotificationCategoryId(
        parsed: PaymentNotificationParseResult,
        categories: List<CategoryEntity>,
    ): Long? {
        val text = listOf(parsed.rawTitle, parsed.rawText).joinToString(" ")
        if (parsed.type == TransactionType.Income && text.contains("退款")) {
            categories.firstOrNull { it.name.contains("退款") }?.let { return it.id }
        }
        if (parsed.type == TransactionType.Expense && text.contains("转账")) {
            categories.firstOrNull { it.name.contains("转账") }?.let { return it.id }
        }
        return categories.firstOrNull()?.id
    }

    private fun TransactionEntity.toListItem(
        categoryName: String,
        categoryIcon: String?,
        accountName: String,
    ): TransactionListItem {
        return TransactionListItem(
            id = id,
            type = type,
            amountCents = amountCents,
            categoryId = categoryId,
            categoryName = categoryName,
            categoryIcon = categoryIcon,
            accountId = accountId,
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
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String? = null,
    val accountId: Long,
    val accountName: String,
    val merchant: String?,
    val note: String?,
    val occurredAt: Long,
    val source: TransactionSource,
    val status: TransactionStatus,
)

data class AccountBalanceSummary(
    val netAssetsCents: Long = 0,
    val totalAssetsCents: Long = 0,
    val totalLiabilitiesCents: Long = 0,
    val accounts: List<AccountBalanceItem> = emptyList(),
)

data class AccountBalanceItem(
    val accountId: Long,
    val accountName: String,
    val accountType: AccountType,
    val balanceCents: Long,
)

data class MonthSummary(
    val incomeCents: Long = 0,
    val expenseCents: Long = 0,
    val balanceCents: Long = 0,
)

data class StatsSnapshot(
    val summary: MonthSummary = MonthSummary(),
    val todayExpenseCents: Long = 0,
    val last7DaysExpenseCents: Long = 0,
    val dailyExpenses: List<DailyExpense> = emptyList(),
    val categoryExpenses: List<CategoryExpense> = emptyList(),
)

data class DailyExpense(
    val label: String,
    val amountCents: Long,
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
    val notificationRules: List<NotificationRuleEntity>,
    val settings: AppSettingEntity?,
)
