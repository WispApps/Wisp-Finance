package com.wisp.finance.ui.settings

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wisp.finance.R
import com.wisp.finance.data.backup.*
import com.wisp.finance.ui.security.*
import java.time.LocalDate
import kotlinx.coroutines.*

@Composable
fun BackupRestoreScreen(backupManager: WispBackupManager,onBack: () -> Unit,modifier: Modifier = Modifier) {
    val scope=rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Uri?>(null) }
    var phrase by remember { mutableStateOf("") }
    var legacy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<BackupResult?>(null) }
    var page by remember { mutableStateOf(0) }
    var safety by remember { mutableStateOf(false) }
    BackHandler(enabled=page!=0) { page=0 }
    if(page==1) { RecoverySetupScreen(backupManager.vault,{page=0},viewOnly=true);return }
    if(page==2) { NextcloudScreen(backupManager,{page=0});return }
    val create=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(WispBackupManager.MIME_TYPE)) { uri ->
        if(uri!=null) {
            busy=true
            scope.launch { try { result=if(safety) backupManager.exportSafetyTo(uri) else backupManager.exportTo(uri) } finally { busy=false } }
        }
    }
    val open=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> pending=uri;phrase="";legacy=false }
    if(pending!=null) {
        SecureScreen()
        AlertDialog(onDismissRequest={if(!busy) pending=null},
            title={Text(stringResource(R.string.backup_restore_confirm_title))},
            text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.sec_restore_warning))
                Row { Checkbox(legacy,{legacy=it});Text(stringResource(R.string.sec_legacy),Modifier.padding(top=12.dp)) }
                if(legacy) Text(stringResource(R.string.sec_legacy_warning),color=MaterialTheme.colorScheme.error)
                else OutlinedTextField(phrase,{phrase=it},label={Text(stringResource(R.string.sec_enter_phrase))},minLines=3,
                    visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password,autoCorrectEnabled=false))
            }},
            confirmButton={TextButton(enabled=!busy && (legacy||phrase.isNotBlank()),onClick={
                val uri=requireNotNull(pending);val text=phrase;val old=legacy
                pending=null;phrase="";busy=true
                scope.launch {try {result=backupManager.restoreFrom(uri,text,old)} finally {busy=false}}
            }) {Text(stringResource(R.string.backup_restore_confirm_action))}},
            dismissButton={TextButton(onClick={pending=null;phrase=""}) {Text(stringResource(R.string.action_cancel))}})
    }
    result?.let { value ->
        AlertDialog(onDismissRequest={result=null},title={Text(stringResource(if(value is BackupResult.Success) R.string.backup_success_title else R.string.backup_error_title))},
            text={Text(if(value is BackupResult.Success) stringResource(R.string.sec_done,value.records) else stringResource(
                if((value as BackupResult.Failure).reason==BackupFailure.WRONG_PHRASE) R.string.sec_wrong_key else R.string.sec_failed))},
            confirmButton={TextButton(onClick={result=null}) {Text(stringResource(R.string.action_ok))}})
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.sec_backup_title),style=MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.sec_backup_intro))
        Button(enabled=!busy,onClick={safety=false;create.launch("WispFinance-${LocalDate.now()}${WispBackupManager.FILE_EXTENSION}")}) {Text(stringResource(R.string.backup_create_action))}
        OutlinedButton(enabled=!busy,onClick={open.launch(arrayOf("*/*"))}) {Text(stringResource(R.string.backup_restore_action))}
        if(backupManager.hasSafetyCopy()) OutlinedButton(enabled=!busy,onClick={safety=true;create.launch("WispFinance-before-restore${WispBackupManager.FILE_EXTENSION}")}) {Text(stringResource(R.string.sec_export_safety))}
        OutlinedButton(enabled=!busy,onClick={page=1}) {Text(stringResource(R.string.sec_show_phrase))}
        OutlinedButton(enabled=!busy,onClick={page=2}) {Text(stringResource(R.string.sec_nextcloud))}
        if(busy) {CircularProgressIndicator();Text(stringResource(R.string.backup_working))}
        Text(stringResource(R.string.sec_scope),style=MaterialTheme.typography.bodySmall)
    }
}
