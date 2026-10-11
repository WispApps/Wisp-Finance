package com.wisp.finance.data.repository

import com.wisp.finance.data.local.dao.CategoryDao
import com.wisp.finance.data.local.mapper.toDomain
import com.wisp.finance.data.local.mapper.toEntity
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.repository.CategoryDeletionResult
import com.wisp.finance.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override fun observeActive(): Flow<List<Category>> {
        return categoryDao.observeActive().map { categories ->
            categories.map { it.toDomain() }
        }
    }

    override fun observeAll(): Flow<List<Category>> {
        return categoryDao.observeAll().map { categories ->
            categories.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): Category? {
        return categoryDao.getById(id)?.toDomain()
    }

    override suspend fun save(category: Category) {
        categoryDao.upsert(category.toEntity())
    }

    override suspend fun setArchived(
        id: String,
        archived: Boolean,
    ) {
        categoryDao.setArchived(
            id = id,
            archived = archived,
        )
    }

    override suspend fun delete(
        id: String,
        replacementCategoryId: String?,
    ): CategoryDeletionResult {
        val category = categoryDao.getById(id)
            ?: return CategoryDeletionResult.DELETED

        if (category.systemKey != null) {
            return CategoryDeletionResult.SYSTEM_CATEGORY_CANNOT_BE_DELETED
        }

        val referenceCount = categoryDao.getReferenceCount(id)

        if (referenceCount > 0 && replacementCategoryId == null) {
            return CategoryDeletionResult.REPLACEMENT_REQUIRED
        }

        if (referenceCount > 0) {
            val replacementId = requireNotNull(replacementCategoryId)
            require(replacementId != id) {
                "Replacement category must differ from the deleted category."
            }

            val replacement = categoryDao.getById(replacementId)
                ?: error("Replacement category does not exist.")

            require(!replacement.archived) {
                "Replacement category must be active."
            }

            categoryDao.replaceReferencesAndDelete(
                categoryId = id,
                replacementCategoryId = replacementId,
            )
        } else {
            categoryDao.deleteById(id)
        }

        return CategoryDeletionResult.DELETED
    }
}
