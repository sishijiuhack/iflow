package com.sishijiuhack.iflow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sishijiuhack.iflow.domain.model.TransactionSource
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index("accountId"),
        Index("occurredAt"),
        Index("status"),
        Index(value = ["rawNotificationId"], unique = true),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amountCents: Long,
    val categoryId: Long,
    val accountId: Long,
    val merchant: String? = null,
    val note: String? = null,
    val occurredAt: Long,
    val source: TransactionSource,
    val status: TransactionStatus,
    val rawNotificationId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
