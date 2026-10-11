package com.wisp.finance.data.backup

import android.content.Context
import android.net.Uri
import android.util.AtomicFile
import androidx.room.withTransaction
import com.wisp.finance.data.local.database.WispFinanceDatabase
import com.wisp.finance.domain.repository.SettingsRepository
import com.wisp.finance.security.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

sealed interface BackupResult {
    data class Success(val records: Int = 0) : BackupResult
    data class Failure(val reason: BackupFailure) : BackupResult
}
enum class BackupFailure { CANNOT_READ, CANNOT_WRITE, INVALID_FILE, UNSUPPORTED_VERSION, INTEGRITY_ERROR, RESTORE_ERROR, WRONG_PHRASE }

class WispBackupManager(private val context: Context, private val database: WispFinanceDatabase,
    private val settingsRepository: SettingsRepository) {
    val vault = RecoveryVault(context)
    val operationMutex = Mutex()
    private val safety = AtomicFile(File(context.noBackupFilesDir,"finance-before-restore.enc"))
    private val dao get() = database.backupDao()
    suspend fun snapshot(): FinanceSnapshot = database.withTransaction {
        FinanceSnapshot.capture(dao,settingsRepository.preferences.first().primaryCurrency)
    }
    suspend fun encryptedSnapshot(): ByteArray = withContext(Dispatchers.IO) {
        RecoveryCrypto.encrypt(snapshot().toJson().toString().toByteArray(Charsets.UTF_8),vault.seed())
    }
    fun decodeEncrypted(bytes: ByteArray, seed: ByteArray): FinanceSnapshot =
        FinanceSnapshot.decode(JSONObject(String(RecoveryCrypto.decrypt(bytes,seed),Charsets.UTF_8)))
    suspend fun exportTo(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            operationMutex.withLock {
                val snapshot=snapshot()
                val bytes=RecoveryCrypto.encrypt(snapshot.toJson().toString().toByteArray(Charsets.UTF_8),vault.seed())
                context.contentResolver.openOutputStream(uri,"wt")?.use { it.write(bytes) } ?: error("Cannot write")
                BackupResult.Success(count(snapshot))
            }
        } catch(e: CancellationException) { throw e } catch(_: Exception) { BackupResult.Failure(BackupFailure.CANNOT_WRITE) }
    }
    fun hasSafetyCopy() = safety.baseFile.exists()
    suspend fun exportSafetyTo(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            operationMutex.withLock {
                val bytes=safety.openRead().use(::readLimited)
                context.contentResolver.openOutputStream(uri,"wt")?.use { it.write(bytes) } ?: error("Cannot write")
                BackupResult.Success()
            }
        } catch(e: CancellationException) { throw e } catch(_: Exception) { BackupResult.Failure(BackupFailure.CANNOT_WRITE) }
    }
    /** Pre-operation snapshot is durably encrypted before any replacement is attempted. */
    private fun writeSafety(snapshot: FinanceSnapshot) {
        val bytes=RecoveryCrypto.encrypt(snapshot.toJson().toString().toByteArray(Charsets.UTF_8),vault.seed())
        val output=safety.startWrite()
        try { output.write(bytes); safety.finishWrite(output) }
        catch(e: Exception) { safety.failWrite(output); throw e }
    }
    suspend fun applySnapshot(next: FinanceSnapshot, expected: FinanceSnapshot? = null, restoreCurrency: Boolean = false) {
        database.withTransaction {
            val current=snapshot()
            if(expected!=null && SnapshotMerge.dataOnly(current.toJson()) != SnapshotMerge.dataOnly(expected.toJson())) throw SyncConflict()
            writeSafety(current)
            dao.replaceSnapshot(next)
            // Only a fresh installation adopts the backed-up explicit currency choice.
            if(restoreCurrency && settingsRepository.preferences.first().primaryCurrency==null)
                next.primaryCurrency?.let { settingsRepository.setPrimaryCurrency(it) }
        }
    }
    suspend fun restoreFrom(uri: Uri, phrase: String, legacy: Boolean = false): BackupResult = withContext(Dispatchers.IO) {
        operationMutex.withLock {
            val bytes=try { context.contentResolver.openInputStream(uri)?.use(::readLimited) ?: error("Cannot read") }
                catch(e: CancellationException) { throw e } catch(_: Exception) { return@withLock BackupResult.Failure(BackupFailure.CANNOT_READ) }
            try {
                val next=if(legacy) {
                    require(!RecoveryCrypto.isEncrypted(bytes))
                    decodeLegacy(bytes,snapshot())
                } else {
                    val seed=try { RecoveryPhrase.parse(phrase) } catch(_: Exception) { return@withLock BackupResult.Failure(BackupFailure.WRONG_PHRASE) }
                    try { decodeEncrypted(bytes,seed) } finally { seed.fill(0) }
                }
                // Invalidate synchronization before replacing data; a storage failure cannot be reported after a successful restore.
                vault.write("sync-invalidated", java.util.UUID.randomUUID().toString().toByteArray(Charsets.UTF_8))
                applySnapshot(next,restoreCurrency=true)
                // The local key stays unchanged: all future copies use the phrase shown in Settings.
                BackupResult.Success(count(next))
            } catch(e: CancellationException) { throw e }
            catch(_: javax.crypto.AEADBadTagException) { BackupResult.Failure(BackupFailure.WRONG_PHRASE) }
            catch(_: Exception) { BackupResult.Failure(BackupFailure.INVALID_FILE) }
        }
    }
    private fun decodeLegacy(bytes: ByteArray,current: FinanceSnapshot): FinanceSnapshot {
        val root=JSONObject(String(bytes,Charsets.UTF_8))
        require(root.getString("format")=="wisp-finance-backup" && root.getInt("version")==1)
        val payload=root.getJSONObject("payload")
        val hash=MessageDigest.getInstance("SHA-256").digest(payload.toString().toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        require(hash==root.getString("sha256"))
        // v1 did not contain these seven tables. Preserve existing advanced finance data.
        val result=current.toJson()
        listOf("categories","purchases","budgets","recurringExpenses","savingsGoals").forEach { result.put(it,payload.getJSONArray(it)) }
        result.put("primaryCurrency",payload.opt("primaryCurrency") ?: JSONObject.NULL)
        return FinanceSnapshot.decode(result)
    }
    private fun count(snapshot: FinanceSnapshot): Int = snapshot.toJson().let { o -> FinanceSnapshot.TABLES.sumOf { o.getJSONArray(it).length() } }
    companion object {
        const val MIME_TYPE="application/octet-stream"
        const val FILE_EXTENSION=".wispfinance.enc"
        fun readLimited(input: InputStream): ByteArray {
            val out=java.io.ByteArrayOutputStream(); val buffer=ByteArray(8192)
            while(true) { val n=input.read(buffer); if(n<0) break; require(out.size()+n <= RecoveryCrypto.MAX_BYTES+52); out.write(buffer,0,n) }
            return out.toByteArray()
        }
    }
}
