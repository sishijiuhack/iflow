package com.sishijiuhack.iflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sishijiuhack.iflow.domain.model.TransactionType

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "type"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val icon: String? = null,
    val sortOrder: Int,
    val isDefault: Boolean,
)
