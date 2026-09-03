package br.com.teclado.update

import java.io.File

class SelfUpdateManager(
    private val releaseSource: ReleaseSource,
    private val apkSource: ApkSource,
    private val installer: InstallerGateway,
    private val checksumMatches: (File, String) -> Boolean = ChecksumVerifier::matches
) {
    fun run(installedVersion: String, onState: (UpdateState) -> Unit) {
        onState(UpdateState.Checking)
        try {
            val release = releaseSource.latest()
            if (!VersionComparator.isNewer(installedVersion, release.version)) {
                onState(UpdateState.UpToDate)
                return
            }

            onState(UpdateState.Available(release.tag))
            onState(UpdateState.Downloading)
            val apk = apkSource.download(release)

            onState(UpdateState.Verifying)
            if (!checksumMatches(apk, release.sha256)) {
                apk.delete()
                onState(UpdateState.Failed("Falha de integridade do APK"))
                return
            }

            when (installer.install(apk)) {
                InstallResult.STARTED -> onState(UpdateState.Installing)
                InstallResult.PERMISSION_REQUIRED -> onState(UpdateState.PermissionRequired)
            }
        } catch (failure: Throwable) {
            onState(UpdateState.Failed(failure.message ?: "Erro desconhecido"))
        }
    }
}
