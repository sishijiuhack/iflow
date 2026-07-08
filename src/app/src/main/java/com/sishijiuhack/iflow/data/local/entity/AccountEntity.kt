package com.sishijiuhack.iflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sishijiuhack.iflow.domain.model.AccountType

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["name"], unique = true)],
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val sortOrder: Int,
    val isDefault: Boolean,
)
