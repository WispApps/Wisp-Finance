package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "loan_payments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loan_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("loan_id"), Index("paid_at")],
)
data class LoanPaymentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "loan_id") val loanId: String,
    @ColumnInfo(name = "amount_minor_units") val amountMinorUnits: Long,
    @ColumnInfo(name = "paid_at") val paidAt: LocalDate,
    val note: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)
