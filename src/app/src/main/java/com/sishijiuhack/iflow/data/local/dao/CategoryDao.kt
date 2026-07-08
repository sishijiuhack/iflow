package com.sishijiuhack.iflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Upsert
    suspend fun upsert(category: CategoryEntity): Long

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY type, sortOrder, id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY type, sortOrder, id")
    suspend fun observeAllSnapshot(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY sortOrder, id")
    suspend fun listByType(type: TransactionType): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
