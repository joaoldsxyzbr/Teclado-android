package br.com.teclado.update

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SelfUpdateManagerTest {
    private val release = ReleaseInfo(
        tag = "v1.1.0",
        version = "1.1.0",
        apkName = "teclado-joaoldsxyzbr-v1.1.0.apk",
        apkUrl = "https://github.com/joaoldsxyzbr/Teclado-android/releases/download/v1.1.0/teclado-joaoldsxyzbr-v1.1.0.apk",
        sha256 = "a".repeat(64),
        size = 100
    )

    @Test fun equalReleaseStopsWithoutDownload() {
        var downloaded = false
        val states = mutableListOf<UpdateState>()
        val manager = SelfUpdateManager(
            releaseSource = ReleaseSource { release.copy(tag = "v1.0.2", version = "1.0.2") },
            apkSource = ApkSource { downloaded = true; File("unused") },
            installer = InstallerGateway { InstallResult.STARTED },
            checksumMatches = { _, _ -> true }
        )

        manager.run("1.0.2", states::add)

        assertEquals(listOf(UpdateState.Checking, UpdateState.UpToDate), states)
        assertFalse(downloaded)
    }

    @Test fun newerReleaseRunsOneTapFlow() {
        val apk = File.createTempFile("update", ".apk")
        val states = mutableListOf<UpdateState>()
        val manager = SelfUpdateManager(
            releaseSource = ReleaseSource { release },
            apkSource = ApkSource { apk },
            installer = InstallerGateway { InstallResult.STARTED },
            checksumMatches = { file, digest -> file == apk && digest == release.sha256 }
        )

        manager.run("1.0.2", states::add)

        assertEquals(
            listOf(
                UpdateState.Checking,
                UpdateState.Available("v1.1.0"),
                UpdateState.Downloading,
                UpdateState.Verifying,
                UpdateState.Installing
            ),
            states
        )
        apk.delete()
    }

    @Test fun digestMismatchFailsClosed() {
        val apk = File.createTempFile("update", ".apk")
        var installerCalled = false
        val states = mutableListOf<UpdateState>()
        val manager = SelfUpdateManager(
            releaseSource = ReleaseSource { release },
            apkSource = ApkSource { apk },
            installer = InstallerGateway { installerCalled = true; InstallResult.STARTED },
            checksumMatches = { _, _ -> false }
        )

        manager.run("1.0.2", states::add)

        assertEquals(UpdateState.Failed("Falha de integridade do APK"), states.last())
        assertFalse(installerCalled)
        apk.delete()
    }

    @Test fun missingInstallPermissionProducesPermissionState() {
        val apk = File.createTempFile("update", ".apk")
        val states = mutableListOf<UpdateState>()
        val manager = SelfUpdateManager(
            releaseSource = ReleaseSource { release },
            apkSource = ApkSource { apk },
            installer = InstallerGateway { InstallResult.PERMISSION_REQUIRED },
            checksumMatches = { _, _ -> true }
        )

        manager.run("1.0.2", states::add)

        assertEquals(UpdateState.PermissionRequired, states.last())
        apk.delete()
    }

    @Test fun networkFailureIsReportedWithoutCrashing() {
        val states = mutableListOf<UpdateState>()
        val manager = SelfUpdateManager(
            releaseSource = ReleaseSource { throw IllegalStateException("offline") },
            apkSource = ApkSource { File("unused") },
            installer = InstallerGateway { InstallResult.STARTED },
            checksumMatches = { _, _ -> true }
        )

        manager.run("1.0.2", states::add)

        assertEquals(UpdateState.Checking, states.first())
        assertEquals(UpdateState.Failed("offline"), states.last())
    }
}
