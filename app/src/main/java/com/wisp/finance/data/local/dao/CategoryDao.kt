package com.wisp.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.wisp.finance.data.local.entity.CategoryEntity
import com.wisp.finance.domain.model.SystemCategoryKey
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query(
        """
        SELECT *
        FROM categories
        WHERE archived = 0
        ORDER BY sort_order ASC, custom_name COLLATE NOCASE ASC
        """
    )
    fun observeActive(): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT *
        FROM categories
        ORDER BY archived ASC, sort_order ASC, custom_name COLLATE NOCASE ASC
        """
    )
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT system_key FROM categories WHERE system_key IS NOT NULL")
    suspend fun getSystemKeys(): List<SystemCategoryKey>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Query("UPDATE categories SET archived = :archived WHERE id = :id")
    suspend fun setArchived(
        id: String,
        archived: Boolean,
    )

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM purchases WHERE category_id = :categoryId) +
            (SELECT COUNT(*) FROM budgets WHERE category_id = :categoryId) +
            (SELECT COUNT(*) FROM recurring_expenses WHERE category_id = :categoryId)
        """
    )
    suspend fun getReferenceCount(categoryId: String): Int

    @Query(
        """
        UPDATE purchases
        SET category_id = :replacementCategoryId
        WHERE category_id = :categoryId
        """
    )
    suspend fun movePurchases(
        categoryId: String,
        replacementCategoryId: String,
    )

    @Query(
        """
        UPDATE budgets
        SET category_id = :replacementCategoryId
        WHERE category_id = :categoryId
        """
    )
    suspend fun moveBudgets(
        categoryId: String,
        replacementCategoryId: String,
    )

    @Query(
        """
        UPDATE recurring_expenses
        SET category_id = :replacementCategoryId
        WHERE category_id = :categoryId
        """
    )
    suspend fun moveRecurringExpenses(
        categoryId: String,
        replacementCategoryId: String,
    )

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    suspend fun replaceReferencesAndDelete(
        categoryId: String,
        replacementCategoryId: String,
    ) {
        require(categoryId != replacementCategoryId) {
            "Replacement category must be different from the deleted category."
        }

        movePurchases(
            categoryId = categoryId,
            replacementCategoryId = replacementCategoryId,
        )
        moveBudgets(
            categoryId = categoryId,
            replacementCategoryId = replacementCategoryId,
        )
        moveRecurringExpenses(
            categoryId = categoryId,
            replacementCategoryId = replacementCategoryId,
        )
        deleteById(categoryId)
    }
}
