package br.com.teclado.update

import java.io.File

fun interface ReleaseSource {
    fun latest(): ReleaseInfo
}

fun interface ApkSource {
    fun download(release: ReleaseInfo): File
}

enum class InstallResult {
    STARTED,
    PERMISSION_REQUIRED
}

fun interface InstallerGateway {
    fun install(apk: File): InstallResult
}
