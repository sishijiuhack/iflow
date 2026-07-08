package com.sishijiuhack.iflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sishijiuhack.iflow.data.local.dao.AccountDao
import com.sishijiuhack.iflow.data.local.dao.AppSettingDao
import com.sishijiuhack.iflow.data.local.dao.CategoryDao
import com.sishijiuhack.iflow.data.local.dao.NotificationRuleDao
import com.sishijiuhack.iflow.data.local.dao.TransactionDao
import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.AppSettingEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.local.entity.NotificationRuleEntity
import com.sishijiuhack.iflow.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        NotificationRuleEntity::class,
        AppSettingEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class IFlowDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun notificationRuleDao(): NotificationRuleDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        private const val DatabaseName = "iflow.db"

        @Volatile
        private var instance: IFlowDatabase? = null

        fun getInstance(context: Context): IFlowDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    IFlowDatabase::class.java,
                    DatabaseName,
                ).build().also { instance = it }
            }
        }
    }
}
