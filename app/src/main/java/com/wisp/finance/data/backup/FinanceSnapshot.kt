package com.wisp.finance.data.backup

import com.wisp.finance.data.local.dao.BackupDao
import com.wisp.finance.data.local.entity.*
import com.wisp.finance.domain.model.*
import java.time.Instant
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

data class FinanceSnapshot(
    val primaryCurrency: CurrencyCode?,
    val categories: List<CategoryEntity>,
    val recurringExpenses: List<RecurringExpenseEntity>,
    val purchases: List<PurchaseEntity>,
    val budgets: List<BudgetEntity>,
    val savingsGoals: List<SavingsGoalEntity>,
    val loans: List<LoanEntity>,
    val loanPayments: List<LoanPaymentEntity>,
    val personalDebts: List<PersonalDebtEntity>,
    val personalDebtPayments: List<PersonalDebtPaymentEntity>,
    val mortgages: List<MortgageEntity>,
    val assetHoldings: List<AssetHoldingEntity>,
    val taxEntries: List<TaxEntryEntity>,
) {
    fun toJson(): JSONObject = JSONObject().put("format", "wisp-finance-snapshot").put("version", 2)
        .put("primaryCurrency", primaryCurrency?.value ?: JSONObject.NULL)
        .put("categories", JSONArray().apply { categories.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("recurringExpenses", JSONArray().apply { recurringExpenses.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("purchases", JSONArray().apply { purchases.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("budgets", JSONArray().apply { budgets.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("savingsGoals", JSONArray().apply { savingsGoals.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("loans", JSONArray().apply { loans.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("loanPayments", JSONArray().apply { loanPayments.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("personalDebts", JSONArray().apply { personalDebts.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("personalDebtPayments", JSONArray().apply { personalDebtPayments.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("mortgages", JSONArray().apply { mortgages.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("assetHoldings", JSONArray().apply { assetHoldings.sortedBy { it.id }.forEach { put(it.toJson()) } })
        .put("taxEntries", JSONArray().apply { taxEntries.sortedBy { it.id }.forEach { put(it.toJson()) } })

    companion object {
        val TABLES = listOf("categories", "recurringExpenses", "purchases", "budgets", "savingsGoals", "loans", "loanPayments", "personalDebts", "personalDebtPayments", "mortgages", "assetHoldings", "taxEntries")
        suspend fun capture(dao: BackupDao, currency: CurrencyCode?) = FinanceSnapshot(currency,
            dao.categories(),
            dao.recurringExpenses(),
            dao.purchases(),
            dao.budgets(),
            dao.savingsGoals(),
            dao.loans(),
            dao.loanPayments(),
            dao.personalDebts(),
            dao.personalDebtPayments(),
            dao.mortgages(),
            dao.assetHoldings(),
            dao.taxEntries(),
)
        fun decode(o: JSONObject): FinanceSnapshot {
            require(o.getString("format") == "wisp-finance-snapshot" && o.getInt("version") == 2)
            val snapshot = FinanceSnapshot(
                o.optString("primaryCurrency").takeIf { it.isNotBlank() && it != "null" }?.let(CurrencyCode::requireCode),
                o.getJSONArray("categories").mapObjects(::categoryFromJson),
                o.getJSONArray("recurringExpenses").mapObjects(::recurringFromJson),
                o.getJSONArray("purchases").mapObjects(::purchaseFromJson),
                o.getJSONArray("budgets").mapObjects(::budgetFromJson),
                o.getJSONArray("savingsGoals").mapObjects(::goalFromJson),
                o.getJSONArray("loans").mapObjects(::loanFromJson),
                o.getJSONArray("loanPayments").mapObjects(::loanPaymentFromJson),
                o.getJSONArray("personalDebts").mapObjects(::personalDebtFromJson),
                o.getJSONArray("personalDebtPayments").mapObjects(::personalDebtPaymentFromJson),
                o.getJSONArray("mortgages").mapObjects(::mortgageFromJson),
                o.getJSONArray("assetHoldings").mapObjects(::assetHoldingFromJson),
                o.getJSONArray("taxEntries").mapObjects(::taxEntryFromJson),
            )
            TABLES.forEach { table ->
                val rows=o.getJSONArray(table); val ids=hashSetOf<String>()
                require(rows.length() <= 100_000)
                for(i in 0 until rows.length()) require(rows.getJSONObject(i).getString("id").let { it.isNotBlank() && ids.add(it) })
            }
            val categories = snapshot.categories.mapTo(hashSetOf()) { it.id }
            require(snapshot.purchases.all { it.categoryId in categories })
            require(snapshot.recurringExpenses.all { it.categoryId in categories })
            require(snapshot.budgets.all { it.categoryId == null || it.categoryId in categories })
            val systemKeys=snapshot.categories.mapNotNull { it.systemKey }
            require(systemKeys.distinct().size == systemKeys.size)
            val loans=snapshot.loans.mapTo(hashSetOf()) { it.id }
            val debts=snapshot.personalDebts.mapTo(hashSetOf()) { it.id }
            require(snapshot.loanPayments.all { it.loanId in loans })
            require(snapshot.personalDebtPayments.all { it.debtId in debts })
            require(snapshot.loans.all { it.annualInterestRate.isFinite() })
            require(snapshot.personalDebts.all { it.annualInterestRate.isFinite() })
            require(snapshot.mortgages.all { it.annualInterestRate.isFinite() })
            require(snapshot.assetHoldings.all { it.quantity.isFinite() })
            return snapshot
        }
    }
}
private fun CategoryEntity.toJson() = JSONObject()
    .put("id", id).put("systemKey", systemKey?.name ?: JSONObject.NULL)
    .put("customName", customName ?: JSONObject.NULL).put("iconKey", iconKey)
    .put("colorArgb", colorArgb).put("archived", archived).put("sortOrder", sortOrder)

private fun RecurringExpenseEntity.toJson() = JSONObject()
    .put("id", id).put("amountMinorUnits", amountMinorUnits).put("currency", currency.value)
    .put("categoryId", categoryId).put("title", title).put("note", note).put("merchant", merchant)
    .put("paymentMethod", paymentMethod.name).put("recurrenceInterval", recurrenceInterval)
    .put("recurrenceUnit", recurrenceUnit.name).put("nextOccurrence", nextOccurrence.toString())
    .put("kind", kind.name).put("enabled", enabled)
    .put("createdAt", createdAt.toString()).put("updatedAt", updatedAt.toString())

private fun PurchaseEntity.toJson() = JSONObject()
    .put("id", id).put("amountMinorUnits", amountMinorUnits).put("currency", currency.value)
    .put("categoryId", categoryId).put("title", title).put("note", note).put("merchant", merchant)
    .put("paymentMethod", paymentMethod.name).put("occurredAt", occurredAt.toString())
    .put("type", type.name).put("recurringExpenseId", recurringExpenseId ?: JSONObject.NULL)
    .put("createdAt", createdAt.toString()).put("updatedAt", updatedAt.toString())

private fun BudgetEntity.toJson() = JSONObject()
    .put("id", id).put("categoryId", categoryId ?: JSONObject.NULL)
    .put("amountMinorUnits", amountMinorUnits).put("currency", currency.value)
    .put("period", period.name).put("enabled", enabled)

private fun SavingsGoalEntity.toJson() = JSONObject()
    .put("id", id).put("title", title).put("targetMinorUnits", targetMinorUnits)
    .put("currency", currency.value).put("savedMinorUnits", savedMinorUnits)
    .put("deadline", deadline?.toString() ?: JSONObject.NULL).put("completed", completed)
    .put("createdAt", createdAt.toString()).put("updatedAt", updatedAt.toString())

private fun categoryFromJson(o: JSONObject) = CategoryEntity(
    id=o.getString("id"), systemKey=o.nullableString("systemKey")?.let(SystemCategoryKey::valueOf),
    customName=o.nullableString("customName"), iconKey=o.getString("iconKey"),
    colorArgb=o.getLong("colorArgb"), archived=o.getBoolean("archived"), sortOrder=o.getInt("sortOrder")
)

private fun recurringFromJson(o: JSONObject) = RecurringExpenseEntity(
    id=o.getString("id"), amountMinorUnits=o.getLong("amountMinorUnits"),
    currency=CurrencyCode.requireCode(o.getString("currency")), categoryId=o.getString("categoryId"),
    title=o.getString("title"), note=o.getString("note"), merchant=o.getString("merchant"),
    paymentMethod=PaymentMethod.valueOf(o.getString("paymentMethod")),
    recurrenceInterval=o.getInt("recurrenceInterval"),
    recurrenceUnit=RecurrenceUnit.valueOf(o.getString("recurrenceUnit")),
    nextOccurrence=LocalDate.parse(o.getString("nextOccurrence")),
    kind=RecurringExpenseKind.valueOf(o.getString("kind")), enabled=o.getBoolean("enabled"),
    createdAt=Instant.parse(o.getString("createdAt")), updatedAt=Instant.parse(o.getString("updatedAt"))
)

private fun purchaseFromJson(o: JSONObject) = PurchaseEntity(
    id=o.getString("id"), amountMinorUnits=o.getLong("amountMinorUnits"),
    currency=CurrencyCode.requireCode(o.getString("currency")), categoryId=o.getString("categoryId"),
    title=o.getString("title"), note=o.getString("note"), merchant=o.getString("merchant"),
    paymentMethod=PaymentMethod.valueOf(o.getString("paymentMethod")),
    occurredAt=Instant.parse(o.getString("occurredAt")), type=PurchaseType.valueOf(o.getString("type")),
    recurringExpenseId=o.nullableString("recurringExpenseId"),
    createdAt=Instant.parse(o.getString("createdAt")), updatedAt=Instant.parse(o.getString("updatedAt"))
)

private fun budgetFromJson(o: JSONObject) = BudgetEntity(
    id=o.getString("id"), categoryId=o.nullableString("categoryId"),
    amountMinorUnits=o.getLong("amountMinorUnits"), currency=CurrencyCode.requireCode(o.getString("currency")),
    period=BudgetPeriod.valueOf(o.getString("period")), enabled=o.getBoolean("enabled")
)

private fun goalFromJson(o: JSONObject) = SavingsGoalEntity(
    id=o.getString("id"), title=o.getString("title"), targetMinorUnits=o.getLong("targetMinorUnits"),
    currency=CurrencyCode.requireCode(o.getString("currency")), savedMinorUnits=o.getLong("savedMinorUnits"),
    deadline=o.nullableString("deadline")?.let(LocalDate::parse), completed=o.getBoolean("completed"),
    createdAt=Instant.parse(o.getString("createdAt")), updatedAt=Instant.parse(o.getString("updatedAt"))
)

private fun JSONObject.nullableString(key: String): String? =
    if (isNull(key)) null else getString(key)

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    List(length()) { index -> transform(getJSONObject(index)) }

private fun LoanEntity.toJson() = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("principalMinorUnits", principalMinorUnits)
    .put("remainingMinorUnits", remainingMinorUnits)
    .put("currency", currency.value)
    .put("annualInterestRate", annualInterestRate)
    .put("termMonths", termMonths)
    .put("startDate", startDate.toString())
    .put("nextPaymentDate", nextPaymentDate?.toString() ?: JSONObject.NULL)
    .put("monthlyPaymentMinorUnits", monthlyPaymentMinorUnits ?: JSONObject.NULL)
    .put("note", note)
    .put("closed", closed)
    .put("createdAt", createdAt.toString())
    .put("updatedAt", updatedAt.toString())

private fun loanFromJson(o: JSONObject) = LoanEntity(
    id = o.getString("id"),
    title = o.getString("title"),
    principalMinorUnits = o.getLong("principalMinorUnits"),
    remainingMinorUnits = o.getLong("remainingMinorUnits"),
    currency = CurrencyCode.requireCode(o.getString("currency")),
    annualInterestRate = o.getDouble("annualInterestRate"),
    termMonths = o.getInt("termMonths"),
    startDate = LocalDate.parse(o.getString("startDate")),
    nextPaymentDate = if (o.isNull("nextPaymentDate")) null else LocalDate.parse(o.getString("nextPaymentDate")),
    monthlyPaymentMinorUnits = if (o.isNull("monthlyPaymentMinorUnits")) null else o.getLong("monthlyPaymentMinorUnits"),
    note = o.getString("note"),
    closed = o.getBoolean("closed"),
    createdAt = Instant.parse(o.getString("createdAt")),
    updatedAt = Instant.parse(o.getString("updatedAt")),
)

private fun LoanPaymentEntity.toJson() = JSONObject()
    .put("id", id)
    .put("loanId", loanId)
    .put("amountMinorUnits", amountMinorUnits)
    .put("paidAt", paidAt.toString())
    .put("note", note)
    .put("createdAt", createdAt.toString())

private fun loanPaymentFromJson(o: JSONObject) = LoanPaymentEntity(
    id = o.getString("id"),
    loanId = o.getString("loanId"),
    amountMinorUnits = o.getLong("amountMinorUnits"),
    paidAt = LocalDate.parse(o.getString("paidAt")),
    note = o.getString("note"),
    createdAt = Instant.parse(o.getString("createdAt")),
)

private fun PersonalDebtEntity.toJson() = JSONObject()
    .put("id", id)
    .put("personName", personName)
    .put("direction", direction.name)
    .put("originalMinorUnits", originalMinorUnits)
    .put("remainingMinorUnits", remainingMinorUnits)
    .put("currency", currency.value)
    .put("dueDate", dueDate?.toString() ?: JSONObject.NULL)
    .put("annualInterestRate", annualInterestRate)
    .put("interestStartDate", interestStartDate?.toString() ?: JSONObject.NULL)
    .put("note", note)
    .put("closed", closed)
    .put("createdAt", createdAt.toString())
    .put("updatedAt", updatedAt.toString())

private fun personalDebtFromJson(o: JSONObject) = PersonalDebtEntity(
    id = o.getString("id"),
    personName = o.getString("personName"),
    direction = DebtDirection.valueOf(o.getString("direction")),
    originalMinorUnits = o.getLong("originalMinorUnits"),
    remainingMinorUnits = o.getLong("remainingMinorUnits"),
    currency = CurrencyCode.requireCode(o.getString("currency")),
    dueDate = if (o.isNull("dueDate")) null else LocalDate.parse(o.getString("dueDate")),
    annualInterestRate = o.getDouble("annualInterestRate"),
    interestStartDate = if (o.isNull("interestStartDate")) null else LocalDate.parse(o.getString("interestStartDate")),
    note = o.getString("note"),
    closed = o.getBoolean("closed"),
    createdAt = Instant.parse(o.getString("createdAt")),
    updatedAt = Instant.parse(o.getString("updatedAt")),
)

private fun PersonalDebtPaymentEntity.toJson() = JSONObject()
    .put("id", id)
    .put("debtId", debtId)
    .put("amountMinorUnits", amountMinorUnits)
    .put("paidAt", paidAt.toString())
    .put("note", note)
    .put("createdAt", createdAt.toString())

private fun personalDebtPaymentFromJson(o: JSONObject) = PersonalDebtPaymentEntity(
    id = o.getString("id"),
    debtId = o.getString("debtId"),
    amountMinorUnits = o.getLong("amountMinorUnits"),
    paidAt = LocalDate.parse(o.getString("paidAt")),
    note = o.getString("note"),
    createdAt = Instant.parse(o.getString("createdAt")),
)

private fun MortgageEntity.toJson() = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("propertyPriceMinorUnits", propertyPriceMinorUnits)
    .put("downPaymentMinorUnits", downPaymentMinorUnits)
    .put("principalMinorUnits", principalMinorUnits)
    .put("currency", currency.value)
    .put("annualInterestRate", annualInterestRate)
    .put("termMonths", termMonths)
    .put("startDate", startDate.toString())
    .put("monthlyPaymentMinorUnits", monthlyPaymentMinorUnits)
    .put("note", note)
    .put("active", active)
    .put("createdAt", createdAt.toString())
    .put("updatedAt", updatedAt.toString())

private fun mortgageFromJson(o: JSONObject) = MortgageEntity(
    id = o.getString("id"),
    title = o.getString("title"),
    propertyPriceMinorUnits = o.getLong("propertyPriceMinorUnits"),
    downPaymentMinorUnits = o.getLong("downPaymentMinorUnits"),
    principalMinorUnits = o.getLong("principalMinorUnits"),
    currency = CurrencyCode.requireCode(o.getString("currency")),
    annualInterestRate = o.getDouble("annualInterestRate"),
    termMonths = o.getInt("termMonths"),
    startDate = LocalDate.parse(o.getString("startDate")),
    monthlyPaymentMinorUnits = o.getLong("monthlyPaymentMinorUnits"),
    note = o.getString("note"),
    active = o.getBoolean("active"),
    createdAt = Instant.parse(o.getString("createdAt")),
    updatedAt = Instant.parse(o.getString("updatedAt")),
)

private fun AssetHoldingEntity.toJson() = JSONObject()
    .put("id", id)
    .put("kind", kind.name)
    .put("name", name)
    .put("symbol", symbol)
    .put("quantity", quantity)
    .put("averageCostMinorUnits", averageCostMinorUnits)
    .put("currentPriceMinorUnits", currentPriceMinorUnits)
    .put("currency", currency.value)
    .put("note", note)
    .put("archived", archived)
    .put("createdAt", createdAt.toString())
    .put("updatedAt", updatedAt.toString())

private fun assetHoldingFromJson(o: JSONObject) = AssetHoldingEntity(
    id = o.getString("id"),
    kind = AssetKind.valueOf(o.getString("kind")),
    name = o.getString("name"),
    symbol = o.getString("symbol"),
    quantity = o.getDouble("quantity"),
    averageCostMinorUnits = o.getLong("averageCostMinorUnits"),
    currentPriceMinorUnits = o.getLong("currentPriceMinorUnits"),
    currency = CurrencyCode.requireCode(o.getString("currency")),
    note = o.getString("note"),
    archived = o.getBoolean("archived"),
    createdAt = Instant.parse(o.getString("createdAt")),
    updatedAt = Instant.parse(o.getString("updatedAt")),
)

private fun TaxEntryEntity.toJson() = JSONObject()
    .put("id", id)
    .put("type", type.name)
    .put("title", title)
    .put("amountMinorUnits", amountMinorUnits)
    .put("currency", currency.value)
    .put("taxYear", taxYear)
    .put("occurredOn", occurredOn.toString())
    .put("note", note)
    .put("createdAt", createdAt.toString())
    .put("updatedAt", updatedAt.toString())

private fun taxEntryFromJson(o: JSONObject) = TaxEntryEntity(
    id = o.getString("id"),
    type = TaxEntryType.valueOf(o.getString("type")),
    title = o.getString("title"),
    amountMinorUnits = o.getLong("amountMinorUnits"),
    currency = CurrencyCode.requireCode(o.getString("currency")),
    taxYear = o.getInt("taxYear"),
    occurredOn = LocalDate.parse(o.getString("occurredOn")),
    note = o.getString("note"),
    createdAt = Instant.parse(o.getString("createdAt")),
    updatedAt = Instant.parse(o.getString("updatedAt")),
)
