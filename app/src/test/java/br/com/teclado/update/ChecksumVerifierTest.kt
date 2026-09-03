package br.com.teclado.update

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecksumVerifierTest {
    @Test fun computesAndMatchesSha256() {
        val file = File.createTempFile("checksum", ".txt")
        try {
            file.writeText("abc")
            val expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
            assertTrue(ChecksumVerifier.matches(file, expected))
            assertTrue(ChecksumVerifier.matches(file, "sha256:$expected"))
            assertFalse(ChecksumVerifier.matches(file, "0".repeat(64)))
        } finally {
            file.delete()
        }
    }
}
