package com.sishijiuhack.iflow.core.di

import android.content.Context
import com.sishijiuhack.iflow.data.local.DefaultDataSeeder
import com.sishijiuhack.iflow.data.local.IFlowDatabase
import com.sishijiuhack.iflow.data.repository.LedgerRepository

class AppContainer(
    val applicationContext: Context,
) {
    val database: IFlowDatabase by lazy {
        IFlowDatabase.getInstance(applicationContext)
    }

    val defaultDataSeeder: DefaultDataSeeder by lazy {
        DefaultDataSeeder(database)
    }

    val ledgerRepository: LedgerRepository by lazy {
        LedgerRepository(database, defaultDataSeeder)
    }
}
