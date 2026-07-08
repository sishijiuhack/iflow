package com.sishijiuhack.iflow.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.sishijiuhack.iflow.data.local.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingDao {
    @Upsert
    suspend fun upsert(setting: AppSettingEntity)

    @Update
    suspend fun update(setting: AppSettingEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun get(): AppSettingEntity?

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observe(): Flow<AppSettingEntity?>
}
