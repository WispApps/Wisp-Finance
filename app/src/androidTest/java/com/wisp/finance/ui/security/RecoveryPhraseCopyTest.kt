package com.wisp.finance.ui.security

import android.content.ClipDescription
import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wisp.finance.R
import com.wisp.finance.security.RecoveryPhrase
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class RecoveryPhraseCopyTest {
    @Test fun copyHasEnglishPairsWithoutNumbersAndSensitiveFlag() {
        // Test-only seed; never uses the real vault or touches the system clipboard.
        val seed = ByteArray(23) { it.toByte() }
        val phrase = RecoveryPhrase.format(seed, "en")
        val clip = recoveryPhraseClip(phrase.split(" "), "Скопировать фразу")
        assertEquals(1, clip.itemCount)
        assertEquals(phrase, clip.getItemAt(0).text.toString())
        assertArrayEquals(seed, RecoveryPhrase.parse(clip.getItemAt(0).text.toString()))
        assertEquals("Скопировать фразу", clip.description.label.toString())
        assertTrue(clip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
        assertTrue(clip.description.extras?.getBoolean("android.content.extra.IS_SENSITIVE") == true)
    }

    @Test fun copyLabelsFollowAllTenSupportedLanguages() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val labels = mapOf(
            "en" to "Copy recovery phrase", "ru" to "Скопировать фразу",
            "uk" to "Скопіювати фразу", "de" to "Wiederherstellungsphrase kopieren",
            "es" to "Copiar frase de recuperación", "fr" to "Copier la phrase de récupération",
            "it" to "Copia la frase di recupero", "nl" to "Herstelzin kopiëren",
            "pl" to "Kopiuj frazę odzyskiwania", "pt" to "Copiar frase de recuperação",
        )
        for ((tag, label) in labels) {
            val configuration = Configuration(context.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(tag))
            }
            val localized = context.createConfigurationContext(configuration)
            assertEquals(label, localized.getString(R.string.setup_copy_phrase))
            assertTrue(localized.getString(R.string.setup_phrase_copied).isNotBlank())
            assertTrue(localized.getString(R.string.setup_copy_warning).isNotBlank())
            assertTrue(localized.getString(R.string.setup_phrase_copy_failed).isNotBlank())
        }
    }
}
