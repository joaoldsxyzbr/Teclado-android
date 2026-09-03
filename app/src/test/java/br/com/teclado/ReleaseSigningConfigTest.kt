package br.com.teclado

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseSigningConfigTest {
    private val gradle = File("build.gradle.kts").readText()
    private val workflow = File("../.github/workflows/android.yml").readText()

    @Test
    fun releaseUsesPermanentSigningSecret() {
        assertTrue(gradle.contains("ANDROID_KEYSTORE_PATH"))
        assertTrue(gradle.contains("ANDROID_KEYSTORE_PASSWORD"))
        assertTrue(workflow.contains("ANDROID_SIGNING_BUNDLE_B64"))
        assertTrue(workflow.contains("assembleRelease"))
        assertTrue(workflow.contains("app-release.apk"))
        assertFalse(workflow.contains("cp app/build/outputs/apk/debug/app-debug.apk"))
    }

    @Test
    fun releaseCertificateFingerprintIsPinned() {
        assertTrue(File("signing/release-cert-sha256.txt").exists())
        assertTrue(workflow.contains("release-cert-sha256.txt"))
        assertTrue(workflow.contains("apksigner"))
    }
}
