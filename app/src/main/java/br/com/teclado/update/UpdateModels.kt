package br.com.teclado.update

data class ReleaseInfo(
    val tag: String,
    val version: String,
    val apkName: String,
    val apkUrl: String,
    val sha256: String,
    val size: Long
)
