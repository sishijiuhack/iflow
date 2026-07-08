package com.sishijiuhack.iflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sishijiuhack.iflow.data.local.entity.TransactionEntity
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoringConflict(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE status != :deletedStatus ORDER BY occurredAt DESC, id DESC")
    fun observeActiveTransactions(deletedStatus: TransactionStatus = TransactionStatus.Deleted): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY occurredAt DESC, id DESC")
    fun observeByStatus(status: TransactionStatus): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY occurredAt DESC, id DESC LIMIT :limit")
    fun observeRecentByStatus(status: TransactionStatus, limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY occurredAt DESC, id DESC")
    suspend fun listByStatus(status: TransactionStatus): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE status != :deletedStatus ORDER BY occurredAt DESC, id DESC")
    suspend fun listActiveTransactions(deletedStatus: TransactionStatus = TransactionStatus.Deleted): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE status != :deletedStatus ORDER BY occurredAt DESC, id DESC LIMIT :limit")
    fun observeRecentActiveTransactions(limit: Int, deletedStatus: TransactionStatus = TransactionStatus.Deleted): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: TransactionStatus, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE status = :status")
    fun observeCountByStatus(status: TransactionStatus): Flow<Int>

}
