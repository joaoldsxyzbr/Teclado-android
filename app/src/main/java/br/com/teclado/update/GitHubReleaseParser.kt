package br.com.teclado.update

import java.net.URL
import org.json.JSONObject

object GitHubReleaseParser {
    fun parse(json: String): ReleaseInfo {
        val root = JSONObject(json)
        val tag = root.getString("tag_name")
        check(Regex("v\\d+(\\.\\d+)*").matches(tag)) { "Invalid release tag" }
        val expectedName = "teclado-joaoldsxyzbr-$tag.apk"
        val assets = root.getJSONArray("assets")
        val matches = (0 until assets.length())
            .map { assets.getJSONObject(it) }
            .filter { it.getString("name") == expectedName }
        check(matches.size == 1) { "Expected APK asset not found" }
        val asset = matches.single()

        check(asset.getString("content_type") == "application/vnd.android.package-archive") {
            "Unexpected APK content type"
        }
        val size = asset.getLong("size")
        check(size > 0) { "Empty APK asset" }

        val url = UpdateUrlPolicy.requireAllowed(URL(asset.getString("browser_download_url")))
        check(asset.has("digest")) { "Release asset has no digest" }
        val digest = asset.getString("digest")
        check(digest.startsWith("sha256:")) { "Release asset digest is not SHA-256" }
        val sha256 = digest.removePrefix("sha256:")
        check(Regex("[0-9a-fA-F]{64}").matches(sha256)) { "Invalid SHA-256 digest" }

        return ReleaseInfo(
            tag = tag,
            version = tag.removePrefix("v"),
            apkName = expectedName,
            apkUrl = url.toString(),
            sha256 = sha256.lowercase(),
            size = size
        )
    }
}
