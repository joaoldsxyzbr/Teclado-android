package br.com.teclado.update

import android.content.Context
import java.io.File
import java.net.URL

class ApkDownloader(
    private val context: Context,
    private val client: SafeHttpClient = SafeHttpClient()
) : ApkSource {
    override fun download(release: ReleaseInfo): File {
        val directory = File(context.cacheDir, "updates")
        check(directory.exists() || directory.mkdirs()) { "Could not create update cache" }
        directory.listFiles()?.forEach { it.delete() }

        val target = File(directory, release.apkName)
        val partial = File(directory, "${release.apkName}.part")
        client.download(URL(release.apkUrl), partial, release.size)

        check(partial.renameTo(target)) { "Could not finalize downloaded APK" }
        return target
    }
}
