package com.sishijiuhack.iflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.sishijiuhack.iflow.data.local.entity.NotificationRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationRuleDao {
    @Upsert
    suspend fun upsert(rule: NotificationRuleEntity): Long

    @Update
    suspend fun update(rule: NotificationRuleEntity)

    @Delete
    suspend fun delete(rule: NotificationRuleEntity)

    @Query("SELECT * FROM notification_rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NotificationRuleEntity?

    @Query("SELECT * FROM notification_rules WHERE enabled = 1 ORDER BY appName, id")
    suspend fun listEnabled(): List<NotificationRuleEntity>

    @Query("SELECT * FROM notification_rules ORDER BY appName, id")
    fun observeAll(): Flow<List<NotificationRuleEntity>>
}
