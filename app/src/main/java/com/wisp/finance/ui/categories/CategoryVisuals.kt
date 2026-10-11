package com.wisp.finance.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R

data class CategoryIconOption(val key: String, val iconRes: Int, val labelRes: Int)
val categoryIconOptions = listOf(
    CategoryIconOption("food", R.drawable.ic_wisp_food, R.string.category_food),
    CategoryIconOption("transport", R.drawable.ic_wisp_transport, R.string.category_transport),
    CategoryIconOption("housing", R.drawable.ic_wisp_home, R.string.category_housing),
    CategoryIconOption("clothing", R.drawable.ic_wisp_clothing, R.string.category_clothing),
    CategoryIconOption("health", R.drawable.ic_wisp_health, R.string.category_health),
    CategoryIconOption("entertainment", R.drawable.ic_wisp_entertainment, R.string.category_entertainment),
    CategoryIconOption("electronics", R.drawable.ic_wisp_electronics, R.string.category_electronics),
    CategoryIconOption("subscriptions", R.drawable.ic_wisp_subscriptions, R.string.category_subscriptions),
    CategoryIconOption("gifts", R.drawable.ic_wisp_gifts, R.string.category_gifts),
    CategoryIconOption("education", R.drawable.ic_wisp_education, R.string.category_education),
    CategoryIconOption("travel", R.drawable.ic_wisp_travel, R.string.category_travel),
    CategoryIconOption("home", R.drawable.ic_wisp_home, R.string.category_home),
    CategoryIconOption("shopping", R.drawable.ic_wisp_shopping, R.string.ui_category_shopping),
    CategoryIconOption("pets", R.drawable.ic_wisp_pets, R.string.ui_category_pets),
    CategoryIconOption("other", R.drawable.ic_wisp_other, R.string.category_other),
)

val categoryColorOptions = listOf(
    0xFFE76F51L,
    0xFF2A9D8FL,
    0xFF457B9DL,
    0xFFE9C46AL,
    0xFFE63946L,
    0xFF9B5DE5L,
    0xFF3A86FFL,
    0xFF8338EFL,
    0xFFFF70A6L,
    0xFF00B4D8L,
    0xFF06D6A0L,
    0xFFF4A261L,
    0xFF6C757DL,
)


@Composable
fun CategoryBadge(iconKey: String, colorArgb: Long, modifier: Modifier = Modifier) {
    val icon = categoryIconOptions.firstOrNull { it.key == iconKey } ?: categoryIconOptions.last()
    val background = Color(colorArgb)
    Box(modifier.size(38.dp).background(background, CircleShape), contentAlignment = Alignment.Center) {
        Icon(painterResource(icon.iconRes), contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (background.luminance() > 0.4f) Color(0xFF111827) else Color.White)
    }
}
