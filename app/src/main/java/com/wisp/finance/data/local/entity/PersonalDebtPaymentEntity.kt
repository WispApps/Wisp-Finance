package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "personal_debt_payments",
    foreignKeys = [
        ForeignKey(
            entity = PersonalDebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debt_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("debt_id"), Index("paid_at")],
)
data class PersonalDebtPaymentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "debt_id") val debtId: String,
    @ColumnInfo(name = "amount_minor_units") val amountMinorUnits: Long,
    @ColumnInfo(name = "paid_at") val paidAt: LocalDate,
    val note: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)
