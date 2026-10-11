package com.wisp.finance.data.local.seed

import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.SystemCategoryKey

object DefaultCategories {

    val all: List<Category> = listOf(
        category(SystemCategoryKey.FOOD, "food", 0xFFE76F51L, 0),
        category(SystemCategoryKey.TRANSPORT, "transport", 0xFF2A9D8FL, 1),
        category(SystemCategoryKey.HOUSING, "housing", 0xFF457B9DL, 2),
        category(SystemCategoryKey.CLOTHING, "clothing", 0xFFE9C46AL, 3),
        category(SystemCategoryKey.HEALTH, "health", 0xFFE63946L, 4),
        category(SystemCategoryKey.ENTERTAINMENT, "entertainment", 0xFF9B5DE5L, 5),
        category(SystemCategoryKey.ELECTRONICS, "electronics", 0xFF3A86FFL, 6),
        category(SystemCategoryKey.SUBSCRIPTIONS, "subscriptions", 0xFF8338EFL, 7),
        category(SystemCategoryKey.GIFTS, "gifts", 0xFFFF70A6L, 8),
        category(SystemCategoryKey.EDUCATION, "education", 0xFF00B4D8L, 9),
        category(SystemCategoryKey.TRAVEL, "travel", 0xFF06D6A0L, 10),
        category(SystemCategoryKey.HOME, "home", 0xFFF4A261L, 11),
        category(SystemCategoryKey.OTHER, "other", 0xFF6C757DL, 12),
    )

    private fun category(
        systemKey: SystemCategoryKey,
        iconKey: String,
        colorArgb: Long,
        sortOrder: Int,
    ): Category {
        return Category(
            id = "system:${systemKey.name.lowercase()}",
            systemKey = systemKey,
            customName = null,
            iconKey = iconKey,
            colorArgb = colorArgb,
            archived = false,
            sortOrder = sortOrder,
        )
    }
}
