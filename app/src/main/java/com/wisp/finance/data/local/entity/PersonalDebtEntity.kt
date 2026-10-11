package com.wisp.finance.data.local.entity
import androidx.room.*
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.DebtDirection
import java.time.Instant
import java.time.LocalDate

@Entity(tableName="personal_debts", indices=[Index("closed"), Index("due_date"), Index("direction")])
data class PersonalDebtEntity(
 @PrimaryKey val id:String,
 @ColumnInfo(name="person_name") val personName:String,
 val direction:DebtDirection,
 @ColumnInfo(name="original_minor_units") val originalMinorUnits:Long,
 @ColumnInfo(name="remaining_minor_units") val remainingMinorUnits:Long,
 val currency:CurrencyCode,
 @ColumnInfo(name="due_date") val dueDate:LocalDate?,
 @ColumnInfo(name="annual_interest_rate") val annualInterestRate:Double,
 @ColumnInfo(name="interest_start_date") val interestStartDate:LocalDate?,
 val note:String,
 val closed:Boolean,
 @ColumnInfo(name="created_at") val createdAt:Instant,
 @ColumnInfo(name="updated_at") val updatedAt:Instant,
)
