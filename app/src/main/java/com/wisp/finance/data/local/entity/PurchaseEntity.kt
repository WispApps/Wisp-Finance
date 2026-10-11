package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.PaymentMethod
import com.wisp.finance.domain.model.PurchaseType
import java.time.Instant

@Entity(
    tableName = "purchases",
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
        Index(value = ["occurred_at"]),
        Index(value = ["recurring_expense_id"]),
    ],
)
data class PurchaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "amount_minor_units")
    val amountMinorUnits: Long,
    val currency: CurrencyCode,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    val title: String,
    val note: String,
    val merchant: String,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: PaymentMethod,
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Instant,
    val type: PurchaseType,
    @ColumnInfo(name = "recurring_expense_id")
    val recurringExpenseId: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
)
