package com.sishijiuhack.iflow.data.local

class DefaultDataSeeder(
    private val database: IFlowDatabase,
) {
    suspend fun seedIfNeeded(nowMillis: Long = System.currentTimeMillis()) {
        val categoryDao = database.categoryDao()
        val accountDao = database.accountDao()
        val notificationRuleDao = database.notificationRuleDao()
        val appSettingDao = database.appSettingDao()

        if (categoryDao.count() == 0) {
            categoryDao.upsertAll(DefaultLedgerData.categories)
        }

        if (accountDao.count() == 0) {
            accountDao.upsertAll(DefaultLedgerData.accounts)
        }

        if (notificationRuleDao.count() == 0) {
            notificationRuleDao.upsertAll(DefaultLedgerData.notificationRules)
        }

        if (appSettingDao.get() == null) {
            appSettingDao.upsert(
                DefaultLedgerData.appSetting.copy(
                    createdAt = nowMillis,
                    updatedAt = nowMillis,
                ),
            )
        }
    }
}
