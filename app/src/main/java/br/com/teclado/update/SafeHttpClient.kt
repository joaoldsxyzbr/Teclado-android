package br.com.teclado.update

import java.io.File
import java.net.HttpURLConnection
import java.net.URL

fun interface ConnectionFactory {
    fun open(url: URL): HttpURLConnection
}

class SafeHttpClient(
    private val connectionFactory: ConnectionFactory = ConnectionFactory { url ->
        url.openConnection() as HttpURLConnection
    }
) {
    fun getText(url: URL): String = withResponse(url) { connection ->
        connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun download(url: URL, target: File, expectedSize: Long) {
        try {
            withResponse(url) { connection ->
                target.outputStream().use { output ->
                    connection.inputStream.use { input -> input.copyTo(output) }
                }
            }
            if (expectedSize > 0) {
                check(target.length() == expectedSize) {
                    "Downloaded APK size mismatch: expected $expectedSize, got ${target.length()}"
                }
            }
        } catch (failure: Throwable) {
            target.delete()
            throw failure
        }
    }

    private fun <T> withResponse(start: URL, consume: (HttpURLConnection) -> T): T {
        var current = UpdateUrlPolicy.requireAllowed(start)
        var redirects = 0

        while (true) {
            val connection = connectionFactory.open(current)
            try {
                connection.instanceFollowRedirects = false
                connection.connectTimeout = CONNECT_TIMEOUT_MS
                connection.readTimeout = READ_TIMEOUT_MS
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("User-Agent", "Teclado-joaoldsxyzbr")

                val code = connection.responseCode
                if (code in REDIRECT_CODES) {
                    check(redirects < MAX_REDIRECTS) { "Too many redirects" }
                    val location = connection.getHeaderField("Location")
                        ?: error("Redirect without Location header")
                    current = UpdateUrlPolicy.requireAllowed(URL(current, location))
                    redirects += 1
                    continue
                }

                check(code in 200..299) { "HTTP request failed with status $code" }
                return consume(connection)
            } finally {
                connection.disconnect()
            }
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 30_000
        const val MAX_REDIRECTS = 5
        val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)
    }
}
