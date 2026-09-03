package br.com.teclado.update

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SafeHttpClientTest {
    private class FakeConnection(
        url: URL,
        private val code: Int,
        private val location: String? = null,
        private val body: String = ""
    ) : HttpURLConnection(url) {
        override fun disconnect() = Unit
        override fun usingProxy(): Boolean = false
        override fun connect() = Unit
        override fun getResponseCode(): Int = code
        override fun getHeaderField(name: String?): String? =
            if (name.equals("Location", ignoreCase = true)) location else null
        override fun getInputStream(): InputStream = ByteArrayInputStream(body.toByteArray())
    }

    @Test fun rejectsRedirectToExternalHostBeforeOpeningIt() {
        val opened = mutableListOf<String>()
        val client = SafeHttpClient(ConnectionFactory { url ->
            opened += url.toString()
            FakeConnection(url, 302, "https://evil.example/file.apk")
        })

        assertThrows(IllegalArgumentException::class.java) {
            client.getText(URL("https://github.com/start"))
        }
        assertEquals(listOf("https://github.com/start"), opened)
    }

    @Test fun followsAllowedGitHubRedirect() {
        val opened = mutableListOf<String>()
        val client = SafeHttpClient(ConnectionFactory { url ->
            opened += url.toString()
            when (opened.size) {
                1 -> FakeConnection(url, 302, "https://objects.githubusercontent.com/file")
                else -> FakeConnection(url, 200, body = "ok")
            }
        })

        assertEquals("ok", client.getText(URL("https://github.com/start")))
        assertEquals(
            listOf("https://github.com/start", "https://objects.githubusercontent.com/file"),
            opened
        )
    }

    @Test fun rejectsTooManyRedirects() {
        val client = SafeHttpClient(ConnectionFactory { url ->
            FakeConnection(url, 302, "https://github.com/again")
        })

        assertThrows(IllegalStateException::class.java) {
            client.getText(URL("https://github.com/start"))
        }
    }
}
