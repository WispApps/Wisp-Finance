package com.wisp.finance.ui.calendar

import com.wisp.finance.ui.common.currentAppLocale

import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.CurrencyCode
import com.wisp.finance.domain.model.Purchase
import com.wisp.finance.domain.model.PurchaseType
import com.wisp.finance.ui.common.categoryDisplayName
import com.wisp.finance.ui.common.formatMinorUnits
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.WeekFields
import java.util.Locale

private data class CalendarCell(
    val date: LocalDate?,
    val hasPurchases: Boolean,
)

@Composable
fun PurchaseCalendarScreen(
    primaryCurrency: CurrencyCode,
    purchases: List<Purchase>,
    categories: List<Category>,
    onBack: () -> Unit,
    onPurchaseClick: (Purchase) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentAppLocale()
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)

    var monthText by remember { mutableStateOf(YearMonth.from(today).toString()) }
    var selectedEpochDay by remember { mutableLongStateOf(today.toEpochDay()) }

    val month = runCatching { YearMonth.parse(monthText) }
        .getOrDefault(YearMonth.from(today))
    val selectedDate = LocalDate.ofEpochDay(selectedEpochDay)
    val categoriesById = categories.associateBy(Category::id)

    val purchasesByDate = remember(purchases) {
        purchases.groupBy { it.occurredAt.atZone(zoneId).toLocalDate() }
    }
    val cells = remember(month, purchasesByDate, locale) {
        calendarCells(month, purchasesByDate.keys, locale)
    }
    val selectedPurchases = purchasesByDate[selectedDate].orEmpty()
        .sortedByDescending(Purchase::occurredAt)

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.calendar_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {
                monthText = YearMonth.from(today).toString()
                selectedEpochDay = today.toEpochDay()
            }) {
                Text(stringResource(R.string.calendar_today))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {
                val previous = month.minusMonths(1)
                monthText = previous.toString()
                selectedEpochDay = previous.atDay(1).toEpochDay()
            }) {
                Icon(painterResource(R.drawable.ic_wisp_back), stringResource(R.string.ui_previous_month))
            }
            Text(
                month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = {
                val next = month.plusMonths(1)
                monthText = next.toString()
                selectedEpochDay = next.atDay(1).toEpochDay()
            }) {
                Icon(painterResource(R.drawable.ic_wisp_chevron), stringResource(R.string.ui_next_month))
            }
        }

        CalendarGrid(
            cells = cells,
            selectedDate = selectedDate,
            today = today,
            locale = locale,
            onDateSelected = { selectedEpochDay = it.toEpochDay() },
        )

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

        Text(
            selectedDate.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
            ),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        if (selectedPurchases.isEmpty()) {
            Text(
                stringResource(R.string.calendar_no_purchases),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(selectedPurchases, key = Purchase::id) { purchase ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onPurchaseClick(purchase)
                        },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    purchase.title.ifBlank {
                                        categoriesById[purchase.categoryId]
                                            ?.let { categoryDisplayName(it) }
                                            ?: stringResource(R.string.analytics_unknown_category)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    categoriesById[purchase.categoryId]
                                        ?.let { categoryDisplayName(it) }
                                        .orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            val prefix = if (purchase.type == PurchaseType.REFUND) "+" else "−"
                            Text(
                                "$prefix${
                                    formatMinorUnits(
                                        purchase.amount.minorUnits,
                                        purchase.amount.currency,
                                        locale,
                                    )
                                }",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (purchase.amount.currency == primaryCurrency) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    cells: List<CalendarCell>,
    selectedDate: LocalDate,
    today: LocalDate,
    locale: Locale,
    onDateSelected: (LocalDate) -> Unit,
) {
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val weekDays = (0L..6L).map { firstDay.plus(it) }

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    day.getDisplayName(java.time.format.TextStyle.SHORT, locale),
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    val date = cell.date
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (date != null) {
                            val selected = date == selectedDate
                            val colors = if (selected) {
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            } else {
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            }

                            Card(
                                colors = colors,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                                    .clickable { onDateSelected(date) },
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            date.dayOfMonth.toString(),
                                            style = if (date == today) {
                                                MaterialTheme.typography.titleSmall
                                            } else {
                                                MaterialTheme.typography.bodyMedium
                                            },
                                        )
                                        if (cell.hasPurchases) {
                                            Text(
                                                "•",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.titleMedium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun calendarCells(
    month: YearMonth,
    datesWithPurchases: Set<LocalDate>,
    locale: Locale,
): List<CalendarCell> {
    val firstDate = month.atDay(1)
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    val leading = (7 + firstDate.dayOfWeek.value - firstDayOfWeek.value) % 7
    val result = mutableListOf<CalendarCell>()

    repeat(leading) {
        result += CalendarCell(null, false)
    }

    for (day in 1..month.lengthOfMonth()) {
        val date = month.atDay(day)
        result += CalendarCell(date, date in datesWithPurchases)
    }

    while (result.size % 7 != 0) {
        result += CalendarCell(null, false)
    }

    return result
}
