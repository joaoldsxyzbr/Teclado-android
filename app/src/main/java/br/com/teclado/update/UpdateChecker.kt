package br.com.teclado.update

import java.net.URL

class UpdateChecker(
    private val client: SafeHttpClient = SafeHttpClient()
) {
    fun latest(): ReleaseInfo = GitHubReleaseParser.parse(
        client.getText(URL(LATEST_RELEASE_URL))
    )

    private companion object {
        const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/joaoldsxyzbr/Teclado-android/releases/latest"
    }
}
