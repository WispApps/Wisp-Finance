package com.wisp.finance.ui.categories

import com.wisp.finance.ui.common.WispScreenHeader
import com.wisp.finance.ui.common.WispDeleteMenu
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.domain.model.Category
import com.wisp.finance.domain.repository.CategoryDeletionResult
import com.wisp.finance.ui.common.categoryDisplayName

@Composable
fun CategoriesScreen(
    categories: List<Category>,
    onBack: () -> Unit,
    onSave: (Category) -> Unit,
    onSetArchived: (Category, Boolean) -> Unit,
    onDelete: (
        category: Category,
        replacementCategoryId: String?,
        onResult: (CategoryDeletionResult) -> Unit,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var creatingCategory by remember { mutableStateOf(false) }
    var replacementFor by remember { mutableStateOf<Category?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    val active = categories.filterNot(Category::archived)
    val archived = categories.filter(Category::archived)

    if (creatingCategory || editingCategory != null) {
        CategoryEditorDialog(
            existingCategory = editingCategory,
            onDismiss = {
                creatingCategory = false
                editingCategory = null
            },
            onSave = {
                onSave(it)
                creatingCategory = false
                editingCategory = null
            },
        )
    }

    replacementFor?.let { category ->
        CategoryReplacementDialog(
            deletedCategory = category,
            candidates = active.filter { it.id != category.id },
            onDismiss = { replacementFor = null },
            onSelected = { replacement ->
                onDelete(category, replacement.id) { result ->
                    if (result == CategoryDeletionResult.DELETED) replacementFor = null
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            
            ,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = stringResource(R.string.categories_title),
                onBack = onBack,
                onAdd = { creatingCategory = true },
            )
        }

        infoMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.categories_active),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        items(active, key = { it.id }) { category ->
            CategoryRow(
                category = category,
                onEdit = { editingCategory = category },
                onArchiveToggle = { onSetArchived(category, true) },
                onDelete = deleteAction(category, active, onDelete) { replacementFor = it }.also {
                    if (it == null && category.systemKey == null) {
                        infoMessage = null
                    }
                },
            )
        }

        if (archived.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.categories_archived),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 18.dp),
                )
            }

            items(archived, key = { "archived:${it.id}" }) { category ->
                CategoryRow(
                    category = category,
                    onEdit = { editingCategory = category },
                    onArchiveToggle = { onSetArchived(category, false) },
                    onDelete = deleteAction(category, active, onDelete) { replacementFor = it },
                )
            }
        }

        item { Spacer(Modifier.size(20.dp)) }
    }
}

private fun deleteAction(
    category: Category,
    active: List<Category>,
    onDelete: (
        category: Category,
        replacementCategoryId: String?,
        onResult: (CategoryDeletionResult) -> Unit,
    ) -> Unit,
    onReplacementRequired: (Category) -> Unit,
): (() -> Unit)? {
    if (category.systemKey != null) return null
    return {
        onDelete(category, null) { result ->
            if (result == CategoryDeletionResult.REPLACEMENT_REQUIRED) onReplacementRequired(category)
        }
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    onAdd: () -> Unit,
) {
    WispScreenHeader(title, onAction = onAdd)
}

@Composable
private fun CategoryRow(
    category: Category,
    onEdit: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CategoryBadge(
                iconKey = category.iconKey,
                colorArgb = category.colorArgb,
                modifier = Modifier.size(52.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryDisplayName(category),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (category.systemKey == null) stringResource(R.string.category_custom) else stringResource(R.string.category_system),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                CategoryActionButton(iconRes = R.drawable.ic_wisp_edit, label = stringResource(R.string.action_edit), onClick = onEdit)
                CategoryActionButton(
                    iconRes = if (category.archived) R.drawable.ic_wisp_restore else R.drawable.ic_wisp_archive,
                    label = stringResource(if (category.archived) R.string.action_restore else R.string.action_archive),
                    onClick = onArchiveToggle,
                )
                onDelete?.let {
                    WispDeleteMenu(onDelete = it)
                }
            }
        }
    }
}

@Composable
private fun CategoryActionButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(painterResource(iconRes), contentDescription = label, modifier = Modifier.size(24.dp))
            }
        }
    }
}
