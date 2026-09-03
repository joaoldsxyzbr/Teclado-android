package br.com.teclado.update

object VersionComparator {
    private fun parts(value: String): List<Int> {
        val clean = value.removePrefix("v")
        require(Regex("\\d+(\\.\\d+)*").matches(clean)) { "Invalid version: $value" }
        return clean.split('.').map(String::toInt)
    }

    fun isNewer(installed: String, candidate: String): Boolean {
        val installedParts = parts(installed)
        val candidateParts = parts(candidate)
        val size = maxOf(installedParts.size, candidateParts.size)
        repeat(size) { index ->
            val current = installedParts.getOrElse(index) { 0 }
            val next = candidateParts.getOrElse(index) { 0 }
            if (next != current) return next > current
        }
        return false
    }
}
