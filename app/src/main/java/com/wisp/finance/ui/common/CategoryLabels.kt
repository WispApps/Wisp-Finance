package com.wisp.finance.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.model.SystemCategoryKey

@Composable
fun categoryDisplayName(category: Category): String {
    category.customName?.let {
        return it
    }

    return when (category.systemKey) {
        SystemCategoryKey.FOOD -> stringResource(R.string.category_food)
        SystemCategoryKey.TRANSPORT -> stringResource(R.string.category_transport)
        SystemCategoryKey.HOUSING -> stringResource(R.string.category_housing)
        SystemCategoryKey.CLOTHING -> stringResource(R.string.category_clothing)
        SystemCategoryKey.HEALTH -> stringResource(R.string.category_health)
        SystemCategoryKey.ENTERTAINMENT -> stringResource(R.string.category_entertainment)
        SystemCategoryKey.ELECTRONICS -> stringResource(R.string.category_electronics)
        SystemCategoryKey.SUBSCRIPTIONS -> stringResource(R.string.category_subscriptions)
        SystemCategoryKey.GIFTS -> stringResource(R.string.category_gifts)
        SystemCategoryKey.EDUCATION -> stringResource(R.string.category_education)
        SystemCategoryKey.TRAVEL -> stringResource(R.string.category_travel)
        SystemCategoryKey.HOME -> stringResource(R.string.category_home)
        SystemCategoryKey.OTHER,
        null,
        -> stringResource(R.string.category_other)
    }
}
