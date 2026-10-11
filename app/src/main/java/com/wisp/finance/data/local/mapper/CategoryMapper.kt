package com.wisp.finance.data.local.mapper

import com.wisp.finance.data.local.entity.CategoryEntity
import com.wisp.finance.domain.model.Category

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        systemKey = systemKey,
        customName = customName,
        iconKey = iconKey,
        colorArgb = colorArgb,
        archived = archived,
        sortOrder = sortOrder,
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        systemKey = systemKey,
        customName = customName,
        iconKey = iconKey,
        colorArgb = colorArgb,
        archived = archived,
        sortOrder = sortOrder,
    )
}
