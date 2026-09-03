package br.com.teclado.update

import org.junit.Assert.assertEquals
import org.junit.Test

class GitHubReleaseParserTest {
    private val digest = "a".repeat(64)

    private fun releaseJson(
        name: String = "teclado-joaoldsxyzbr-v1.1.0.apk",
        contentType: String = "application/vnd.android.package-archive",
        url: String = "https://github.com/joaoldsxyzbr/Teclado-android/releases/download/v1.1.0/teclado-joaoldsxyzbr-v1.1.0.apk",
        includeDigest: Boolean = true
    ): String {
        val digestField = if (includeDigest) ",\"digest\":\"sha256:$digest\"" else ""
        return """{
            \"tag_name\":\"v1.1.0\",
            \"assets\":[{
                \"name\":\"$name\",
                \"content_type\":\"$contentType\",
                \"browser_download_url\":\"$url\",
                \"size\":1234$digestField
            }]
        }""".replace("\\\"", "\"")
    }

    @Test fun parsesExpectedApkAsset() {
        val release = GitHubReleaseParser.parse(releaseJson())
        assertEquals("v1.1.0", release.tag)
        assertEquals("1.1.0", release.version)
        assertEquals("teclado-joaoldsxyzbr-v1.1.0.apk", release.apkName)
        assertEquals(digest, release.sha256)
        assertEquals(1234L, release.size)
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsMissingDigest() {
        GitHubReleaseParser.parse(releaseJson(includeDigest = false))
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsWrongAssetName() {
        GitHubReleaseParser.parse(releaseJson(name = "other.apk"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsExternalDownloadHost() {
        GitHubReleaseParser.parse(releaseJson(url = "https://evil.example/app.apk"))
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsWrongMimeType() {
        GitHubReleaseParser.parse(releaseJson(contentType = "application/octet-stream"))
    }
}
