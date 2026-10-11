package com.wisp.finance.ui.settings

import androidx.activity.compose.BackHandler
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
import com.wisp.finance.sync.*
import com.wisp.finance.security.ThreeWayMerge
import com.wisp.finance.ui.security.SecureScreen
import kotlinx.coroutines.*

@Composable
fun NextcloudScreen(backups: WispBackupManager,onBack: () -> Unit) {
    SecureScreen()
    val sync=remember(backups){NextcloudSync(backups)}
    val scope=rememberCoroutineScope()
    var url by remember {mutableStateOf("")}
    var user by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var phrase by remember {mutableStateOf("")}
    var busy by remember {mutableStateOf(false)}
    var message by remember {mutableStateOf<Int?>(null)}
    var status by remember {mutableIntStateOf(0)}
    var conflict by remember {mutableStateOf(false)}
    fun synchronize(policy: Int) {
        busy=true;message=null;conflict=false
        scope.launch {
            try {
                withContext(Dispatchers.IO){sync.save(url,user,password,phrase)}
                sync.sync(policy);message=R.string.sec_cloud_success
            } catch(e: CancellationException){throw e}
            catch(_: SyncConflict){message=R.string.sec_cloud_conflict;conflict=true}
            catch(e: CloudHttpError){status=e.status;message=R.string.sec_cloud_http}
            catch(_:javax.crypto.AEADBadTagException){message=R.string.sec_wrong_key}
            catch(_:Exception){message=R.string.sec_cloud_failed}
            finally{busy=false}
        }
    }
    if(conflict) AlertDialog(onDismissRequest={conflict=false},
        title={Text(stringResource(R.string.sec_resolve_title))},
        text={Text(stringResource(R.string.sec_resolve_body))},
        confirmButton={Column {
            TextButton(onClick={synchronize(ThreeWayMerge.LOCAL)}) {Text(stringResource(R.string.sec_keep_local))}
            TextButton(onClick={synchronize(ThreeWayMerge.REMOTE)}) {Text(stringResource(R.string.sec_keep_cloud))}
        }},dismissButton={TextButton(onClick={conflict=false}) {Text(stringResource(R.string.action_cancel))}})
    LaunchedEffect(sync) {
        try {withContext(Dispatchers.IO){sync.config()}?.let {url=it.url;user=it.username;password=it.password}}
        catch(e: CancellationException){throw e} catch(_:Exception){message=R.string.sec_failed}
    }
    BackHandler {if(!busy)onBack()}
    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text(stringResource(R.string.sec_nextcloud),style=MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.sec_cloud_intro))
        OutlinedTextField(url,{url=it},Modifier.fillMaxWidth(),enabled=!busy,label={Text(stringResource(R.string.sec_cloud_url))},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Uri,autoCorrectEnabled=false))
        Text("https://cloud.example.com/remote.php/dav/files/USERNAME/",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(user,{user=it},Modifier.fillMaxWidth(),enabled=!busy,label={Text(stringResource(R.string.sec_cloud_user))},singleLine=true,keyboardOptions=KeyboardOptions(autoCorrectEnabled=false))
        OutlinedTextField(password,{password=it},Modifier.fillMaxWidth(),enabled=!busy,label={Text(stringResource(R.string.sec_cloud_password))},singleLine=true,visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password,autoCorrectEnabled=false))
        OutlinedTextField(phrase,{phrase=it},Modifier.fillMaxWidth(),enabled=!busy,label={Text(stringResource(R.string.sec_cloud_phrase))},minLines=2,visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password,autoCorrectEnabled=false))
        Text(stringResource(R.string.sec_cloud_phrase_hint),style=MaterialTheme.typography.bodySmall)
        Button(enabled=!busy&&url.isNotBlank()&&user.isNotBlank()&&password.isNotBlank(),onClick={
            synchronize(ThreeWayMerge.FAIL)
        }) {Text(stringResource(R.string.sec_sync))}
        Text(stringResource(R.string.sec_cloud_manual),style=MaterialTheme.typography.bodySmall)
        OutlinedButton(enabled=!busy,onClick={
            scope.launch {try {withContext(Dispatchers.IO){sync.disconnect()};url="";user="";password="";phrase="";message=R.string.sec_disconnected}
                catch(e:CancellationException){throw e} catch(_:Exception){message=R.string.sec_failed}}
        }) {Text(stringResource(R.string.sec_disconnect))}
        if(busy) CircularProgressIndicator()
        message?.let {Text(if(it==R.string.sec_cloud_http) stringResource(it,status) else stringResource(it))}
        TextButton(enabled=!busy,onClick=onBack) {Text(stringResource(R.string.action_close))}
    }
}
