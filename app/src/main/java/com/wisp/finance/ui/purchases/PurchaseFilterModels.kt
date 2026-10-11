package com.wisp.finance.ui.purchases

import com.wisp.finance.domain.model.PaymentMethod
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.domain.model.PurchaseType
import java.time.ZoneId
import java.util.Locale

enum class PurchaseSort {
    NEWEST,
    OLDEST,
    AMOUNT_HIGH,
    AMOUNT_LOW,
    TITLE,
}

data class PurchaseFilters(
    val categoryId: String? = null,
    val currencyCode: String? = null,
    val paymentMethod: PaymentMethod? = null,
    val type: PurchaseType? = null,
    val merchantQuery: String = "",
    val minMinorUnits: Long? = null,
    val maxMinorUnits: Long? = null,
    val startEpochDay: Long? = null,
    val endEpochDay: Long? = null,
)

fun filterAndSortPurchases(
    purchases: List<Purchase>,
    searchQuery: String,
    filters: PurchaseFilters,
    sort: PurchaseSort,
    categoryNamesById: Map<String, String>,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<Purchase> {
    val normalizedSearch = searchQuery
        .trim()
        .lowercase(locale)

    val normalizedMerchant = filters.merchantQuery
        .trim()
        .lowercase(locale)

    val filtered = purchases.filter { purchase ->
        val categoryName = categoryNamesById[purchase.categoryId].orEmpty()

        val matchesSearch = normalizedSearch.isBlank() ||
            purchase.title.lowercase(locale).contains(normalizedSearch) ||
            purchase.note.lowercase(locale).contains(normalizedSearch) ||
            purchase.merchant.lowercase(locale).contains(normalizedSearch) ||
            categoryName.lowercase(locale).contains(normalizedSearch)

        val matchesCategory =
            filters.categoryId == null ||
                purchase.categoryId == filters.categoryId

        val matchesCurrency =
            filters.currencyCode == null ||
                purchase.amount.currency.value == filters.currencyCode

        val matchesPayment =
            filters.paymentMethod == null ||
                purchase.paymentMethod == filters.paymentMethod

        val matchesType =
            filters.type == null ||
                purchase.type == filters.type

        val matchesMerchant =
            normalizedMerchant.isBlank() ||
                purchase.merchant
                    .lowercase(locale)
                    .contains(normalizedMerchant)

        val localEpochDay = purchase.occurredAt
            .atZone(zoneId)
            .toLocalDate()
            .toEpochDay()

        val matchesStart =
            filters.startEpochDay == null ||
                localEpochDay >= filters.startEpochDay

        val matchesEnd =
            filters.endEpochDay == null ||
                localEpochDay <= filters.endEpochDay

        val amountFilterCanApply =
            filters.currencyCode != null &&
                purchase.amount.currency.value == filters.currencyCode

        val matchesMin =
            filters.minMinorUnits == null ||
                (
                    amountFilterCanApply &&
                        purchase.amount.minorUnits >= filters.minMinorUnits
                )

        val matchesMax =
            filters.maxMinorUnits == null ||
                (
                    amountFilterCanApply &&
                        purchase.amount.minorUnits <= filters.maxMinorUnits
                )

        matchesSearch &&
            matchesCategory &&
            matchesCurrency &&
            matchesPayment &&
            matchesType &&
            matchesMerchant &&
            matchesStart &&
            matchesEnd &&
            matchesMin &&
            matchesMax
    }

    val effectiveSort = if (
        sort == PurchaseSort.AMOUNT_HIGH ||
        sort == PurchaseSort.AMOUNT_LOW
    ) {
        if (filters.currencyCode == null) {
            PurchaseSort.NEWEST
        } else {
            sort
        }
    } else {
        sort
    }

    return when (effectiveSort) {
        PurchaseSort.NEWEST -> filtered.sortedByDescending(Purchase::occurredAt)
        PurchaseSort.OLDEST -> filtered.sortedBy(Purchase::occurredAt)
        PurchaseSort.AMOUNT_HIGH -> filtered.sortedByDescending {
            it.amount.minorUnits
        }
        PurchaseSort.AMOUNT_LOW -> filtered.sortedBy {
            it.amount.minorUnits
        }
        PurchaseSort.TITLE -> filtered.sortedWith(
            compareBy(
                String.CASE_INSENSITIVE_ORDER,
            ) {
                it.title.ifBlank { it.merchant }
            },
        )
    }
}
