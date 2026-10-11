package com.wisp.finance.domain.repository

enum class CategoryDeletionResult {
    DELETED,
    REPLACEMENT_REQUIRED,
    SYSTEM_CATEGORY_CANNOT_BE_DELETED,
}
