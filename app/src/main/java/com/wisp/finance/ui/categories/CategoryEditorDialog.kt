package com.wisp.finance.ui.categories

import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.ui.common.categoryDisplayName
import java.util.UUID

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
    existingCategory: Category?,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit,
) {
    val defaultDisplayName = existingCategory?.let {
        categoryDisplayName(it)
    }.orEmpty()

    var name by remember(existingCategory?.id) {
        mutableStateOf(defaultDisplayName)
    }
    var iconKey by remember(existingCategory?.id) {
        mutableStateOf(existingCategory?.iconKey ?: "other")
    }
    var colorArgb by remember(existingCategory?.id) {
        mutableLongStateOf(existingCategory?.colorArgb ?: 0xFF6C757DL)
    }
    var nameError by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (existingCategory == null) {
                        R.string.category_create
                    } else {
                        R.string.category_edit
                    },
                ),
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.category_name))
                    },
                    singleLine = true,
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text(stringResource(R.string.category_name_required))
                        }
                    },
                )

                Text(
                    text = stringResource(R.string.category_icon),
                    style = MaterialTheme.typography.labelLarge,
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoryIconOptions.forEach { option ->
                        FilterChip(
                            modifier = Modifier.heightIn(min = 48.dp),
                            selected = iconKey == option.key,
                            onClick = { iconKey = option.key },
                            label = { Icon(painterResource(option.iconRes),
                                contentDescription = stringResource(option.labelRes),
                                modifier = Modifier.size(24.dp)) },
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.category_color),
                    style = MaterialTheme.typography.labelLarge,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    categoryColorOptions.chunked(7).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { option ->
                                Box(
                                    modifier = Modifier
                                        .size(
                                            if (option == colorArgb) {
                                                38.dp
                                            } else {
                                                32.dp
                                            },
                                        )
                                        .background(
                                            color = Color(option),
                                            shape = CircleShape,
                                        )
                                        .clickable {
                                            colorArgb = option
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (option == colorArgb) {
                                        Text(
                                            text = "✓",
                                            color = Color.White,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (existingCategory?.systemKey != null) {
                    Text(
                        text = stringResource(
                            R.string.category_system_edit_note,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cleanName = name.trim()

                    if (cleanName.isBlank()) {
                        nameError = true
                        return@TextButton
                    }

                    val customName = when {
                        existingCategory == null -> cleanName
                        existingCategory.systemKey == null -> cleanName
                        existingCategory.customName == null &&
                            cleanName == defaultDisplayName -> null
                        else -> cleanName
                    }

                    onSave(
                        Category(
                            id = existingCategory?.id
                                ?: UUID.randomUUID().toString(),
                            systemKey = existingCategory?.systemKey,
                            customName = customName,
                            iconKey = iconKey,
                            colorArgb = colorArgb,
                            archived = existingCategory?.archived ?: false,
                            sortOrder = existingCategory?.sortOrder
                                ?: Int.MAX_VALUE,
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
