package com.wisp.finance.data.local.database

import androidx.room.TypeConverter
import com.wisp.finance.domain.model.BudgetPeriod
import com.wisp.finance.domain.model.TaxEntryType
import com.wisp.finance.domain.model.AssetKind
import com.wisp.finance.domain.model.DebtDirection
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.PaymentMethod
import com.wisp.finance.domain.model.PurchaseType
import com.wisp.finance.domain.model.RecurrenceUnit
import com.wisp.finance.domain.model.RecurringExpenseKind
import com.wisp.finance.domain.model.SystemCategoryKey
import java.time.Instant
import java.time.LocalDate

class DatabaseConverters {
    @TypeConverter fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
    @TypeConverter fun localDateToEpochDay(value: LocalDate?): Long? = value?.toEpochDay()
    @TypeConverter fun epochDayToLocalDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)
    @TypeConverter fun currencyCodeToString(value: CurrencyCode?): String? = value?.value
    @TypeConverter fun stringToCurrencyCode(value: String?): CurrencyCode? = value?.let(CurrencyCode::fromCode)
    @TypeConverter fun paymentMethodToString(value: PaymentMethod?): String? = value?.name
    @TypeConverter fun stringToPaymentMethod(value: String?): PaymentMethod? = value?.let(PaymentMethod::valueOf)
    @TypeConverter fun purchaseTypeToString(value: PurchaseType?): String? = value?.name
    @TypeConverter fun stringToPurchaseType(value: String?): PurchaseType? = value?.let(PurchaseType::valueOf)
    @TypeConverter fun budgetPeriodToString(value: BudgetPeriod?): String? = value?.name
    @TypeConverter fun stringToBudgetPeriod(value: String?): BudgetPeriod? = value?.let(BudgetPeriod::valueOf)
    @TypeConverter fun recurrenceUnitToString(value: RecurrenceUnit?): String? = value?.name
    @TypeConverter fun stringToRecurrenceUnit(value: String?): RecurrenceUnit? = value?.let(RecurrenceUnit::valueOf)
    @TypeConverter fun recurringExpenseKindToString(value: RecurringExpenseKind?): String? = value?.name
    @TypeConverter fun stringToRecurringExpenseKind(value: String?): RecurringExpenseKind? = value?.let(RecurringExpenseKind::valueOf)
    @TypeConverter fun debtDirectionToString(value: DebtDirection?): String? = value?.name
    @TypeConverter fun stringToDebtDirection(value: String?): DebtDirection? = value?.let(DebtDirection::valueOf)
    @TypeConverter fun assetKindToString(value: AssetKind?): String? = value?.name
    @TypeConverter fun stringToAssetKind(value: String?): AssetKind? = value?.let(AssetKind::valueOf)
    @TypeConverter fun taxEntryTypeToString(value: TaxEntryType?): String? = value?.name
    @TypeConverter fun stringToTaxEntryType(value: String?): TaxEntryType? = value?.let(TaxEntryType::valueOf)
    @TypeConverter fun systemCategoryKeyToString(value: SystemCategoryKey?): String? = value?.name
    @TypeConverter fun stringToSystemCategoryKey(value: String?): SystemCategoryKey? = value?.let(SystemCategoryKey::valueOf)
}
