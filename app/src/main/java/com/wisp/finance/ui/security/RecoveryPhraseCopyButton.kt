package com.wisp.finance.ui.security

import android.content.ClipData
import android.content.ClipboardManager
import android.os.PersistableBundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wisp.finance.R

/** Plain English pairs only: no row numbers or localized UI text in the payload. */
internal fun recoveryPhraseClip(words: List<String>, label: String): ClipData {
    require(words.size == 24)
    return ClipData.newPlainText(label, words.joinToString(" ")).apply {
        description.extras = PersistableBundle().apply {
            // ClipDescription.EXTRA_IS_SENSITIVE's value also works below API 33.
            // Supporting Android versions hide the clipboard preview, not access to the clip.
            putBoolean("android.content.extra.IS_SENSITIVE", true)
        }
    }
}

@Composable
internal fun RecoveryPhraseCopyButton(words: List<String>, enabled: Boolean) {
    val context = LocalContext.current
    val copyLabel = stringResource(R.string.setup_copy_phrase)
    var copied by remember(words) { mutableStateOf<Boolean?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = {
                copied = runCatching {
                    val clipboard = requireNotNull(context.getSystemService(ClipboardManager::class.java))
                    clipboard.setPrimaryClip(recoveryPhraseClip(words, copyLabel))
                }.isSuccess
            },
            enabled = enabled,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) { Text(copyLabel) }
        Text(
            stringResource(R.string.setup_copy_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        copied?.let { success ->
            Text(
                stringResource(if (success) R.string.setup_phrase_copied else R.string.setup_phrase_copy_failed),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium,
                color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
    }
}
