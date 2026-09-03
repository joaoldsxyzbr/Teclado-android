package br.com.teclado.update

import java.io.File
import java.security.MessageDigest

object ChecksumVerifier {
    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun matches(file: File, expected: String): Boolean {
        val normalized = expected.removePrefix("sha256:")
        require(Regex("[0-9a-fA-F]{64}").matches(normalized)) { "Invalid SHA-256 digest" }
        return sha256(file).equals(normalized, ignoreCase = true)
    }
}
