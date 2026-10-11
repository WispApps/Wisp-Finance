package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.PaymentMethod
import com.wisp.finance.domain.model.RecurrenceUnit
import com.wisp.finance.domain.model.RecurringExpenseKind
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "recurring_expenses",
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
        Index(value = ["next_occurrence"]),
        Index(value = ["enabled"]),
        Index(value = ["kind"]),
    ],
)
data class RecurringExpenseEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "amount_minor_units") val amountMinorUnits: Long,
    val currency: CurrencyCode,
    @ColumnInfo(name = "category_id") val categoryId: String,
    val title: String,
    val note: String,
    val merchant: String,
    @ColumnInfo(name = "payment_method") val paymentMethod: PaymentMethod,
    @ColumnInfo(name = "recurrence_interval") val recurrenceInterval: Int,
    @ColumnInfo(name = "recurrence_unit") val recurrenceUnit: RecurrenceUnit,
    @ColumnInfo(name = "next_occurrence") val nextOccurrence: LocalDate,
    val kind: RecurringExpenseKind,
    val enabled: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
