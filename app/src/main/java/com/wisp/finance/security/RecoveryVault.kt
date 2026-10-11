package com.wisp.finance.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Only the device-wrapped seed/credentials live here. Portable archives never use this key. */
class RecoveryVault(context: Context) {
    private val dir = File(context.noBackupFilesDir, "security-v1").apply { mkdirs() }
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        return generator.generateKey()
    }
    @Synchronized fun read(name: String): ByteArray? {
        val file = AtomicFile(File(dir, name))
        if (!file.baseFile.exists() && !File(dir, "$name.bak").exists()) return null
        val data = file.openRead().use { it.readBytes() }
        require(data.size >= 28)
        return Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, data.copyOfRange(0,12)))
            updateAAD(name.toByteArray(Charsets.UTF_8))
            doFinal(data,12,data.size-12)
        }
    }
    @Synchronized fun write(name: String, data: ByteArray) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()); updateAAD(name.toByteArray(Charsets.UTF_8)) }
        val file=AtomicFile(File(dir,name)); val output=file.startWrite()
        try { output.write(cipher.iv + cipher.doFinal(data)); file.finishWrite(output) }
        catch (e: Exception) { file.failWrite(output); throw e }
    }
    @Synchronized fun seed(): ByteArray = requireNotNull(read("recovery"))
    @Synchronized fun isReady(): Boolean = runCatching { seed().size == 23 }.getOrDefault(false)
    @Synchronized fun install(seed: ByteArray) { require(seed.size==23); write("recovery",seed) }
    companion object { private const val ALIAS="wisp-finance-device-v1" }
}
