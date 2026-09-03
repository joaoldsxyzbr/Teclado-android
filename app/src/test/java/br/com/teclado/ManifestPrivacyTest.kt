package br.com.teclado

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestPrivacyTest {
    private val manifest = File("src/main/AndroidManifest.xml").readText()

    @Test
    fun updaterPermissionsAreExplicit() {
        assertTrue(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android.permission.REQUEST_INSTALL_PACKAGES"))
    }

    @Test
    fun updateProviderIsPrivate() {
        assertTrue(manifest.contains(".update.fileprovider"))
        assertTrue(manifest.contains("android:exported=\"false\""))
        assertTrue(manifest.contains("android:grantUriPermissions=\"true\""))
    }

    @Test
    fun noTelemetrySdkMarkersInProductionSources() {
        val source = File("src/main").walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.name.endsWith(".kts")) }
            .joinToString("\n") { it.readText() }
        assertFalse(source.contains("Firebase"))
        assertFalse(source.contains("Analytics"))
    }
}
