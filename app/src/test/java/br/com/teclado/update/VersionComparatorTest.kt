package br.com.teclado.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {
    @Test fun newerPatchIsDetected() {
        assertTrue(VersionComparator.isNewer("1.0.2", "v1.0.3"))
    }

    @Test fun equalVersionIsNotNewer() {
        assertFalse(VersionComparator.isNewer("1.0.2", "1.0.2"))
    }

    @Test fun olderCandidateIsNotNewer() {
        assertFalse(VersionComparator.isNewer("1.2.0", "v1.1.9"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun malformedCandidateIsRejected() {
        VersionComparator.isNewer("1.0.2", "latest")
    }
}
