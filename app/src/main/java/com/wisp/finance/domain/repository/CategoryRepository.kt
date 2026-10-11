package com.wisp.finance.domain.repository

import com.wisp.finance.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun observeActive(): Flow<List<Category>>

    fun observeAll(): Flow<List<Category>>

    suspend fun getById(id: String): Category?

    suspend fun save(category: Category)

    suspend fun setArchived(
        id: String,
        archived: Boolean,
    )

    suspend fun delete(
        id: String,
        replacementCategoryId: String? = null,
    ): CategoryDeletionResult
}
