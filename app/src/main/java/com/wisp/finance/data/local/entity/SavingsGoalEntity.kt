package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wisp.finance.domain.model.CurrencyCode
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "savings_goals",
    indices = [
        Index(value = ["completed"]),
        Index(value = ["deadline"]),
    ],
)
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "target_minor_units") val targetMinorUnits: Long,
    val currency: CurrencyCode,
    @ColumnInfo(name = "saved_minor_units") val savedMinorUnits: Long,
    val deadline: LocalDate?,
    val completed: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
