package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wisp.finance.domain.model.BudgetPeriod
import com.wisp.finance.domain.model.CurrencyCode

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["category_id"]),
    ],
)
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String?,
    @ColumnInfo(name = "amount_minor_units")
    val amountMinorUnits: Long,
    val currency: CurrencyCode,
    val period: BudgetPeriod,
    val enabled: Boolean,
)
