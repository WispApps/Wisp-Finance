package com.wisp.finance.sync

import android.util.Base64
import com.wisp.finance.data.backup.*
import com.wisp.finance.security.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

class CloudHttpError(val status: Int) : Exception()
data class NextcloudConfig(val url: String, val username: String, val password: String, val seed: ByteArray)

class NextcloudSync(private val backups: WispBackupManager) {
    private val vault get() = backups.vault
    private val client=OkHttpClient.Builder().followRedirects(false).followSslRedirects(false)
        .connectTimeout(15,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).writeTimeout(30,TimeUnit.SECONDS)
        .callTimeout(60,TimeUnit.SECONDS).build()
    fun config(): NextcloudConfig? = vault.read("nextcloud")?.let {
        if (String(it,Charsets.UTF_8)=="null") return@let null
        val o=JSONObject(String(it,Charsets.UTF_8))
        NextcloudConfig(o.getString("url"),o.getString("username"),o.getString("password"),Base64.decode(o.getString("seed"),Base64.NO_WRAP))
    }
    fun save(url: String, username: String, password: String, phrase: String) {
        val parsed=url.trim().trimEnd('/').toHttpUrl()
        require(parsed.scheme=="https" && parsed.username.isEmpty() && parsed.password.isEmpty() && parsed.query==null && parsed.fragment==null)
        require(username.isNotBlank() && !username.contains(':') && password.isNotEmpty())
        val prior=runCatching { config() }.getOrNull()
        val seed=if(phrase.isNotBlank()) RecoveryPhrase.parse(phrase)
            else if(prior!=null && prior.url==parsed.toString().trimEnd('/') && prior.username==username.trim()) prior.seed
            else vault.seed()
        vault.write("nextcloud",JSONObject().put("url",parsed.toString().trimEnd('/')).put("username",username.trim())
            .put("password",password).put("seed",Base64.encodeToString(seed,Base64.NO_WRAP)).toString().toByteArray(Charsets.UTF_8))
    }
    fun disconnect() { vault.write("nextcloud", "null".toByteArray()) }
    private fun request(c: NextcloudConfig, name: String): Request.Builder {
        val url=c.url.toHttpUrl().newBuilder().addPathSegment("WispFinance").apply { if(name.isNotEmpty()) addPathSegment(name) }.build()
        return Request.Builder().url(url).header("Authorization",Credentials.basic(c.username,c.password,Charsets.UTF_8))
    }
    private fun execute(request: Request): Response = client.newCall(request).execute()
    private fun put(c: NextcloudConfig,name: String,bytes: ByteArray,etag: String? = null) {
        val req=request(c,name).put(bytes.toRequestBody("application/octet-stream".toMediaType()))
            .header(if(etag==null) "If-None-Match" else "If-Match",etag ?: "*").build()
        execute(req).use { if(it.code==412) throw SyncConflict(); if(!it.isSuccessful) throw CloudHttpError(it.code) }
    }
    private fun baselineName(c: NextcloudConfig): String {
        val digest=MessageDigest.getInstance("SHA-256")
        digest.update((c.url+"\n"+c.username).toByteArray(Charsets.UTF_8)); digest.update(c.seed)
        vault.read("sync-invalidated")?.let { digest.update(it) }
        return "sync-base-"+digest.digest().joinToString("") { "%02x".format(it) }
    }
    suspend fun sync(policy: Int = ThreeWayMerge.FAIL) = withContext(Dispatchers.IO) {
        backups.operationMutex.withLock {
            val c=requireNotNull(config())
            execute(request(c,"").method("MKCOL",ByteArray(0).toRequestBody()).build()).use {
                if(!it.isSuccessful && it.code!=405) throw CloudHttpError(it.code)
            }
            ensureActive()
            val name=baselineName(c)
            val base=vault.read(name)?.let { JSONObject(String(it,Charsets.UTF_8)) }
            val local=backups.snapshot()
            var etag: String?=null
            val remoteBytes=execute(request(c,FILE).get().build()).use {
                if(it.code==404) null else {
                    if(!it.isSuccessful) throw CloudHttpError(it.code)
                    etag=it.header("ETag")?.takeUnless { tag -> tag.startsWith("W/") }
                    require(!etag.isNullOrBlank())
                    it.body?.byteStream()?.use(WispBackupManager::readLimited) ?: error("Empty response")
                }
            }
            if(remoteBytes==null && base!=null && policy!=ThreeWayMerge.LOCAL) throw SyncConflict()
            val remote=remoteBytes?.let { backups.decodeEncrypted(it,c.seed) }
            val merged=if(remote==null) local else FinanceSnapshot.decode(SnapshotMerge.merge(local.toJson(),remote.toJson(),base,policy))
            val encrypted=RecoveryCrypto.encrypt(merged.toJson().toString().toByteArray(Charsets.UTF_8),c.seed)
            ensureActive()
            // Immutable encrypted history protects both sides before conditional replacement.
            if(remoteBytes!=null) put(c,"history-remote-${UUID.randomUUID()}.enc",remoteBytes)
            put(c,"history-device-${UUID.randomUUID()}.enc",RecoveryCrypto.encrypt(local.toJson().toString().toByteArray(Charsets.UTF_8),c.seed))
            put(c,FILE,encrypted,etag)
            // If local edits arrived while networking, do not replace them. The next retry merges again.
            withContext(NonCancellable) {
                backups.applySnapshot(merged,expected=local)
                vault.write(name,merged.toJson().toString().toByteArray(Charsets.UTF_8))
            }
        }
    }
    companion object { const val FILE="finance.wispfinance.enc" }
}
