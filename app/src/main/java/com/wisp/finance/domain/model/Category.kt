package com.wisp.finance.domain.model

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val systemKey: SystemCategoryKey? = null,
    val customName: String? = null,
    val iconKey: String,
    val colorArgb: Long,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
) {

    init {
        require(id.isNotBlank()) {
            "Category ID cannot be blank."
        }

        require(iconKey.isNotBlank()) {
            "Category icon key cannot be blank."
        }

        require(colorArgb in 0L..0xFFFFFFFFL) {
            "Category color must be a valid ARGB value."
        }

        require(sortOrder >= 0) {
            "Category sort order cannot be negative."
        }

        if (systemKey == null) {
            require(!customName.isNullOrBlank()) {
                "A custom category must have a name."
            }
        }

        if (customName != null) {
            require(customName.isNotBlank()) {
                "Category custom name cannot be blank."
            }
        }
    }
}
