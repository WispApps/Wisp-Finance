package com.wisp.finance.ui.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.security.*
import com.wisp.finance.ui.onboarding.OnboardingLayout
import com.wisp.finance.ui.onboarding.SetupButton
import kotlinx.coroutines.*
import java.security.MessageDigest

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

@Composable
fun SecureScreen() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.activity()?.window
        val wasSecure = (window?.attributes?.flags ?: 0) and WindowManager.LayoutParams.FLAG_SECURE != 0
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { if (!wasSecure) window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

@Composable
fun RecoverySetupScreen(vault: RecoveryVault, onFinished: () -> Unit, viewOnly: Boolean = false) {
    SecureScreen()
    val scope = rememberCoroutineScope()
    // Secret bytes and input deliberately never enter rememberSaveable / saved-state Bundles.
    var seed by remember(vault, viewOnly) { mutableStateOf<ByteArray?>(null) }
    var loadFailed by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(vault, viewOnly, attempt) {
        loadFailed = false
        try {
            seed = withContext(Dispatchers.IO) {
                if (viewOnly) vault.seed() else vault.read("pending-recovery")
                    ?: RecoveryPhrase.generate().also { vault.write("pending-recovery", it) }
            }
        } catch (e: CancellationException) { throw e }
          catch (_: Exception) { loadFailed = true }
    }
    val words = remember(seed) { seed?.let { RecoveryPhrase.format(it, "en").trim().split(Regex("\\s+")) } }
    RecoverySetupContent(
        words = words, viewOnly = viewOnly, loadFailed = loadFailed, error = error, busy = busy,
        onRetry = { attempt++ }, onClearError = { error = false }, onFinished = onFinished,
        onConfirm = { input, imported ->
            busy = true
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val parsed = RecoveryPhrase.parse(input)
                        if (!imported) require(MessageDigest.isEqual(parsed, requireNotNull(seed)))
                        vault.install(parsed)
                    }
                    onFinished()
                } catch (e: CancellationException) { throw e }
                  catch (_: Exception) { error = true }
                finally { busy = false }
            }
        },
    )
}

/** Presentation shared with the safe preview. No access to the vault, database or preferences. */
@Composable
internal fun RecoverySetupContent(
    words: List<String>?,
    onFinished: () -> Unit,
    onConfirm: (String, Boolean) -> Unit,
    viewOnly: Boolean = false,
    loadFailed: Boolean = false,
    error: Boolean = false,
    busy: Boolean = false,
    onRetry: () -> Unit = {},
    onClearError: () -> Unit = {},
    preview: Boolean = false,
    onClosePreview: () -> Unit = {},
) {
    var imported by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    BackHandler(enabled = viewOnly || confirming || imported || busy) {
        if (!busy) {
            if (viewOnly) onFinished()
            else { confirming = false; imported = false; input = ""; saved = false; onClearError() }
        }
    }
    OnboardingLayout(
        step = if (viewOnly) null else 2,
        title = stringResource(if (confirming) R.string.setup_verify_title else if (imported) R.string.sec_enter_phrase else R.string.sec_title),
        subtitle = stringResource(if (preview) R.string.setup_demo_phrase else R.string.setup_phrase_subtitle),
        preview = preview, onClosePreview = onClosePreview,
        actions = {
            when {
                viewOnly -> SetupButton(stringResource(R.string.action_close), onClick = onFinished)
                !imported && !confirming -> SetupButton(stringResource(R.string.sec_continue),
                    enabled = saved && words != null && !loadFailed, onClick = { confirming = true })
                else -> SetupButton(stringResource(R.string.sec_continue), busy = busy,
                    enabled = saved && !busy && (preview || input.isNotBlank()) && (imported || words != null),
                    onClick = { onConfirm(input, imported) })
            }
            if (!viewOnly) TextButton(
                enabled = !busy,
                onClick = {
                    if (confirming) { confirming = false }
                    else { imported = !imported; saved = false }
                    input = ""; onClearError()
                }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(stringResource(if (confirming) R.string.setup_back_phrase else if (imported) R.string.sec_generate else R.string.sec_have_phrase))
            }
        },
    ) {
        if (!confirming) {
            item(key = "warning") {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.errorContainer) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.setup_keep_private), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.sec_warning), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (!imported && !loadFailed && words != null) item(key = "copy-phrase") {
                RecoveryPhraseCopyButton(words = words, enabled = !busy)
            }
            if (!imported) item(key = "phrase") {
                when {
                    loadFailed -> Column {
                        Text(stringResource(R.string.sec_failed), color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onRetry) { Text(stringResource(R.string.setup_retry)) }
                    }
                    words == null -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    else -> RecoveryWordGrid(words)
                }
            }
            item(key = "format") { Text(stringResource(R.string.sec_format), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        if (!viewOnly && (confirming || imported)) item(key = "confirmation") {
            OutlinedTextField(
                value = input, onValueChange = { input = it; onClearError() }, enabled = !busy,
                modifier = Modifier.fillMaxWidth(), minLines = 3,
                label = { Text(stringResource(if (imported) R.string.sec_enter_phrase else R.string.sec_confirm_phrase)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                isError = error,
            )
        }
        if (error) item(key = "error") { Text(stringResource(R.string.sec_phrase_error), color = MaterialTheme.colorScheme.error) }
        if (!viewOnly && !confirming) item(key = "saved") {
            Row(Modifier.fillMaxWidth().toggleable(value = saved, enabled = !busy, role = Role.Checkbox, onValueChange = { saved = it })
                .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = saved, onCheckedChange = null)
                Text(stringResource(R.string.sec_saved), Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (!confirming) item(key = "scope") {
            Text(stringResource(R.string.sec_scope), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecoveryWordGrid(words: List<String>) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val wordStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
    val numberStyle = MaterialTheme.typography.labelSmall
    val wordWidth = remember(words, wordStyle, density, measurer) {
        words.maxOfOrNull { measurer.measure(AnnotatedString(it), style = wordStyle, softWrap = false).size.width } ?: 0
    }
    val numberWidth = with(density) {
        measurer.measure(AnnotatedString("24"), style = numberStyle, softWrap = false).size.width.toDp()
    }.coerceAtLeast(26.dp)
    val minimumCellWidth = with(density) { wordWidth.toDp() } + numberWidth + 28.dp
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < minimumCellWidth * 2 + 8.dp) 1 else 2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            words.chunked(columns).forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEachIndexed { columnIndex, word ->
                        Surface(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${rowIndex * columns + columnIndex + 1}", Modifier.width(numberWidth),
                                    style = numberStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(word, style = wordStyle)
                            }
                        }
                    }
                    if (row.size < columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
