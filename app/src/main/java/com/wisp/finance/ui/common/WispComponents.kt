package com.wisp.finance.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.wisp.finance.R

@Composable
fun WispPrimaryButton(onClick: () -> Unit, modifier: Modifier = Modifier,
    label: String = stringResource(R.string.action_add_short), iconRes: Int = R.drawable.ic_wisp_add) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary)) {
        Icon(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun WispScreenHeader(title: String, onAction: () -> Unit, modifier: Modifier = Modifier,
    actionLabel: String = stringResource(R.string.action_add_short), actionIcon: Int = R.drawable.ic_wisp_add) {
    val titleStyle = MaterialTheme.typography.titleLarge
    val labelStyle = MaterialTheme.typography.labelLarge
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        val needed = measurer.measure(title, titleStyle, softWrap = false).size.width +
            measurer.measure(actionLabel, labelStyle, softWrap = false).size.width +
            with(density) { 84.dp.roundToPx() }
        if (needed <= with(density) { maxWidth.roundToPx() }) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(title, Modifier.weight(1f), style = titleStyle)
                WispPrimaryButton(onAction, label = actionLabel, iconRes = actionIcon)
            }
        } else {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = titleStyle)
                WispPrimaryButton(onAction, Modifier.align(Alignment.End), actionLabel, actionIcon)
            }
        }
    }
}

@Composable
fun WispEmptyState(title: String, body: String? = null, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            body?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge) }
        }
    }
}

@Composable
fun WispDeleteMenu(onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.ic_wisp_other),
                contentDescription = stringResource(R.string.ui_more_actions))
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.action_delete),
                color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_wisp_delete), null,
                    tint = MaterialTheme.colorScheme.error) },
                onClick = { expanded = false; confirming = true })
        }
    }
    if (confirming) AlertDialog(onDismissRequest = { confirming = false },
        title = { Text(stringResource(R.string.ui_delete_title)) },
        text = { Text(stringResource(R.string.ui_delete_body)) },
        confirmButton = { TextButton(onClick = { confirming = false; onDelete() }) {
            Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
        } },
        dismissButton = { TextButton(onClick = { confirming = false }) {
            Text(stringResource(R.string.action_cancel))
        } })
}
