package com.sishijiuhack.iflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_rules",
    indices = [Index("packageName")],
)
data class NotificationRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val enabled: Boolean,
    val keywords: List<String>,
    val amountPattern: String,
    val directionPattern: String,
    val merchantPattern: String? = null,
)
