package com.sishijiuhack.iflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    val id: Int = DefaultId,
    val autoCaptureEnabled: Boolean,
    val autoConfirmEnabled: Boolean,
    val defaultAccountId: Long,
    val lastExportedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        const val DefaultId = 1
    }
}
