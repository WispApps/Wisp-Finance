package com.wisp.finance.data.local.seed

import com.wisp.finance.data.local.dao.CategoryDao
import com.wisp.finance.data.local.mapper.toEntity

class DefaultCategorySeeder(
    private val categoryDao: CategoryDao,
) {

    suspend fun seedIfNeeded() {
        val existingSystemKeys = categoryDao
            .getSystemKeys()
            .toSet()

        val missingCategories = DefaultCategories.all
            .filter { it.systemKey !in existingSystemKeys }
            .map { it.toEntity() }

        if (missingCategories.isNotEmpty()) {
            categoryDao.insertAll(missingCategories)
        }
    }
}
