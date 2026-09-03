package br.com.teclado.update

import java.net.URL

object UpdateUrlPolicy {
    fun requireAllowed(url: URL): URL {
        require(url.protocol.equals("https", ignoreCase = true)) { "Only HTTPS is allowed" }
        val host = url.host.lowercase()
        require(
            host == "api.github.com" ||
                host == "github.com" ||
                host.endsWith(".githubusercontent.com")
        ) { "Host not allowed: $host" }
        return url
    }
}
