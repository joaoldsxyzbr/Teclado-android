package br.com.teclado.update

import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateUrlPolicyTest {
    @Test fun allowsApprovedGitHubHosts() {
        listOf(
            "https://api.github.com/repos/joaoldsxyzbr/Teclado-android/releases/latest",
            "https://github.com/joaoldsxyzbr/Teclado-android/releases/download/v1.1.0/app.apk",
            "https://release-assets.githubusercontent.com/file",
            "https://objects.githubusercontent.com/file"
        ).forEach { value ->
            assertEquals(value, UpdateUrlPolicy.requireAllowed(URL(value)).toString())
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPlainHttp() {
        UpdateUrlPolicy.requireAllowed(URL("http://github.com/file"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsExternalHost() {
        UpdateUrlPolicy.requireAllowed(URL("https://evil.example/file"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsLookalikeHost() {
        UpdateUrlPolicy.requireAllowed(URL("https://github.com.evil.example/file"))
    }
}
