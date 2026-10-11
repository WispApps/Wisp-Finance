package com.wisp.finance.data.local.entity
import androidx.room.*
import com.wisp.finance.domain.model.CurrencyCode
import java.time.Instant
import java.time.LocalDate

@Entity(tableName="loans", indices=[Index("closed"), Index("next_payment_date")])
data class LoanEntity(
 @PrimaryKey val id:String,
 val title:String,
 @ColumnInfo(name="principal_minor_units") val principalMinorUnits:Long,
 @ColumnInfo(name="remaining_minor_units") val remainingMinorUnits:Long,
 val currency:CurrencyCode,
 @ColumnInfo(name="annual_interest_rate") val annualInterestRate:Double,
 @ColumnInfo(name="term_months") val termMonths:Int,
 @ColumnInfo(name="start_date") val startDate:LocalDate,
 @ColumnInfo(name="next_payment_date") val nextPaymentDate:LocalDate?,
 @ColumnInfo(name="monthly_payment_minor_units") val monthlyPaymentMinorUnits:Long?,
 val note:String,
 val closed:Boolean,
 @ColumnInfo(name="created_at") val createdAt:Instant,
 @ColumnInfo(name="updated_at") val updatedAt:Instant,
)
