package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.TaxEntryType
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "tax_entries", indices = [Index("tax_year"), Index("type"), Index("occurred_on")])
data class TaxEntryEntity(
    @PrimaryKey val id: String,
    val type: TaxEntryType,
    val title: String,
    @ColumnInfo(name = "amount_minor_units") val amountMinorUnits: Long,
    val currency: CurrencyCode,
    @ColumnInfo(name = "tax_year") val taxYear: Int,
    @ColumnInfo(name = "occurred_on") val occurredOn: LocalDate,
    val note: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
