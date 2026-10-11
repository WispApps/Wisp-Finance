package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wisp.finance.domain.model.SystemCategoryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(
            value = ["system_key"],
            unique = true,
        ),
    ],
)
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "system_key")
    val systemKey: SystemCategoryKey?,
    @ColumnInfo(name = "custom_name")
    val customName: String?,
    @ColumnInfo(name = "icon_key")
    val iconKey: String,
    @ColumnInfo(name = "color_argb")
    val colorArgb: Long,
    val archived: Boolean,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
)
