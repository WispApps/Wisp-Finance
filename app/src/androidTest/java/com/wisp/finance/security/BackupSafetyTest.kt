package com.wisp.finance.security

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wisp.finance.data.backup.*
import com.wisp.finance.data.local.database.WispFinanceDatabase
import com.wisp.finance.domain.model.*
import com.wisp.finance.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest
import java.util.UUID

/** Uses a separate in-memory database and test-only storage, never the installed app database. */
@RunWith(AndroidJUnit4::class)
class BackupSafetyTest {
    private lateinit var db: WispFinanceDatabase
    private lateinit var backups: WispBackupManager
    private lateinit var folder: File
    private lateinit var original: FinanceSnapshot
    private val seed=ByteArray(23) { it.toByte() }
    @Before fun setup() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext
        folder=File(app.cacheDir,"backup-test-${UUID.randomUUID()}").apply {mkdirs()}
        val context=object:ContextWrapper(app) { override fun getNoBackupFilesDir()=folder }
        db=Room.inMemoryDatabaseBuilder(context,WispFinanceDatabase::class.java).build()
        val settings=object:SettingsRepository {
            override val preferences=MutableStateFlow(UserPreferences(CurrencyCode.requireCode("PLN")))
            override suspend fun setPrimaryCurrency(currency:CurrencyCode) {preferences.value=UserPreferences(currency)}
        }
        backups=WispBackupManager(context,db,settings)
        backups.vault.install(seed)
        original=FinanceSnapshot.decode(JSONObject(instrumentation.context.assets.open("full_snapshot.json").bufferedReader().use {it.readText()}))
    }
    @After fun close() { db.close();folder.deleteRecursively() }
    private suspend fun data()=SnapshotMerge.dataOnly(backups.snapshot().toJson())
    @Test fun allTwelveTablesSurviveEncryptedRoundTrip()=runBlocking {
        backups.applySnapshot(original)
        val before=data()
        val archive=File(folder,"roundtrip.enc").apply {writeBytes(backups.encryptedSnapshot())}
        val empty=original.toJson().apply {FinanceSnapshot.TABLES.forEach {put(it,org.json.JSONArray())}}
        db.backupDao().replaceSnapshot(FinanceSnapshot.decode(empty))
        val result=backups.restoreFrom(Uri.fromFile(archive),RecoveryPhrase.format(seed,"ru"))
        assertTrue(result is BackupResult.Success)
        assertEquals(before,data())
        assertEquals(9223372036854775800L,backups.snapshot().purchases.single().amountMinorUnits)
    }
    @Test fun wrongPhraseAndCorruptionNeverReplaceRecords()=runBlocking {
        backups.applySnapshot(original)
        val before=data();val bytes=backups.encryptedSnapshot()
        val file=File(folder,"damaged.enc").apply {writeBytes(bytes)}
        assertTrue(backups.restoreFrom(Uri.fromFile(file),RecoveryPhrase.format(RecoveryPhrase.generate(),"en")) is BackupResult.Failure)
        assertEquals(before,data())
        bytes[bytes.lastIndex]=(bytes.last().toInt() xor 1).toByte();file.writeBytes(bytes)
        assertTrue(backups.restoreFrom(Uri.fromFile(file),RecoveryPhrase.format(seed,"de")) is BackupResult.Failure)
        assertEquals(before,data())
    }
    @Test fun foreignKeyFailureRollsBackAllTables()=runBlocking {
        backups.applySnapshot(original);val before=data()
        val invalid=original.copy(purchases=original.purchases.map {it.copy(categoryId="missing")})
        assertTrue(runCatching {backups.applySnapshot(invalid)}.isFailure)
        assertEquals(before,data())
    }
    @Test fun legacyImportPreservesAdvancedTables()=runBlocking {
        backups.applySnapshot(original);val before=data()
        val payload=original.toJson().apply {
            remove("format");remove("version");put("languageTag","ru")
            listOf("loans","loanPayments","personalDebts","personalDebtPayments","mortgages","assetHoldings","taxEntries").forEach {remove(it)}
        }
        val hash=MessageDigest.getInstance("SHA-256").digest(payload.toString().toByteArray(Charsets.UTF_8)).joinToString("") {"%02x".format(it)}
        val root=JSONObject().put("format","wisp-finance-backup").put("version",1).put("payload",payload).put("sha256",hash)
        val file=File(folder,"legacy.wispfinance").apply {writeText(root.toString())}
        assertTrue(backups.restoreFrom(Uri.fromFile(file),"",legacy=true) is BackupResult.Success)
        assertEquals(before,data())
    }
}
