package com.wisp.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wisp.finance.domain.model.CurrencyCode
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "mortgages", indices = [Index("active"), Index("start_date")])
data class MortgageEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "property_price_minor_units") val propertyPriceMinorUnits: Long,
    @ColumnInfo(name = "down_payment_minor_units") val downPaymentMinorUnits: Long,
    @ColumnInfo(name = "principal_minor_units") val principalMinorUnits: Long,
    val currency: CurrencyCode,
    @ColumnInfo(name = "annual_interest_rate") val annualInterestRate: Double,
    @ColumnInfo(name = "term_months") val termMonths: Int,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "monthly_payment_minor_units") val monthlyPaymentMinorUnits: Long,
    val note: String,
    val active: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
