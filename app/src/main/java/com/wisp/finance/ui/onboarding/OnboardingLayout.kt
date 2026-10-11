package com.wisp.finance.ui.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import java.util.Locale

/** A local language preview. It never changes the persisted application locale. */
@Composable
internal fun OnboardingLocale(languageTag: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val original = LocalConfiguration.current
    val configuration = remember(original, languageTag) {
        Configuration(original).apply { setLocale(Locale.forLanguageTag(languageTag)) }
    }
    val localizedContext = remember(context, configuration) {
        context.createConfigurationContext(configuration)
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration,
        content = content,
    )
}

/** All headings and content scroll together, including with large accessibility fonts. */
@Composable
internal fun OnboardingLayout(
    step: Int?,
    title: String,
    subtitle: String,
    preview: Boolean = false,
    onClosePreview: () -> Unit = {},
    actions: @Composable ColumnScope.() -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxSize()) {
                if (preview) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.setup_preview_label), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = onClosePreview) { Text(stringResource(R.string.action_close)) }
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "heading") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Image(painterResource(R.drawable.wisp_finance_launcher), contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)))
                                Text("Wisp Finance", style = MaterialTheme.typography.titleMedium)
                            }
                            if (step != null) {
                                Text(stringResource(R.string.setup_step, step, 4), style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    repeat(4) { index ->
                                        Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                                            .background(if (index < step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant))
                                    }
                                }
                            }
                            Text(title, style = MaterialTheme.typography.headlineMedium)
                            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    content()
                }
                Surface(shadowElevation = 3.dp, color = MaterialTheme.colorScheme.background) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp), content = actions)
                }
            }
        }
    }
}

@Composable
internal fun SetupButton(text: String, onClick: () -> Unit, enabled: Boolean = true, busy: Boolean = false) {
    Button(onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
        }
        Text(text, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
internal fun SetupChoice(title: String, subtitle: String? = null, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp).heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}
