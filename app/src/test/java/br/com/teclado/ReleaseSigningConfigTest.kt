package br.com.teclado

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseSigningConfigTest {
    private val gradle = File("build.gradle.kts").readText()
    private val ciWorkflow = File("../.github/workflows/android.yml").readText()
    private val releaseWorkflow = File("../.github/workflows/release.yml").readText()

    @Test
    fun releaseUsesPermanentSigningSecret() {
        assertTrue(gradle.contains("ANDROID_KEYSTORE_PATH"))
        assertTrue(gradle.contains("ANDROID_KEYSTORE_PASSWORD"))
        assertTrue(releaseWorkflow.contains("ANDROID_SIGNING_BUNDLE_B64"))
        assertTrue(releaseWorkflow.contains("assembleRelease"))
        assertTrue(releaseWorkflow.contains("app-release.apk"))
        assertFalse(releaseWorkflow.contains("cp app/build/outputs/apk/debug/app-debug.apk"))
    }

    @Test
    fun releaseSigningIsSeparatedFromMainCi() {
        assertFalse(ciWorkflow.contains("ANDROID_SIGNING_BUNDLE_B64"))
        assertFalse(ciWorkflow.contains("assembleRelease"))
        assertTrue(ciWorkflow.contains("Upload debug APK"))
        assertTrue(releaseWorkflow.contains("tags:"))
        assertTrue(releaseWorkflow.contains("- 'v*'"))
    }

    @Test
    fun releaseCertificateFingerprintIsPinned() {
        assertTrue(File("signing/release-cert-sha256.txt").exists())
        assertTrue(releaseWorkflow.contains("release-cert-sha256.txt"))
        assertTrue(releaseWorkflow.contains("apksigner"))
    }

    @Test
    fun releaseCertificateParsingDoesNotDependOnExactApksignerPrefix() {
        assertTrue(releaseWorkflow.contains("CERT_OUTPUT=\$(\$APKSIGNER verify --print-certs \"\$APK\")"))
        assertTrue(releaseWorkflow.contains("grep -iE 'certificate.*sha-?256.*digest'"))
        assertFalse(releaseWorkflow.contains("sed -n 's/^Signer #1 certificate SHA-256 digest: //p'"))
    }
}
