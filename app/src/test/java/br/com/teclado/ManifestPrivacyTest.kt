package br.com.teclado

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class ManifestPrivacyTest {
    @Test
    fun manifestDoesNotRequestInternetPermission() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("android.permission.INTERNET"))
    }
}
