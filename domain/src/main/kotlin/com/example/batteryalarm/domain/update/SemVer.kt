package com.example.batteryalarm.domain.update

/**
 * Stable SemVer core: MAJOR.MINOR.PATCH only.
 * Tags may include a leading `v`. Prerelease/build suffixes are rejected.
 */
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
) : Comparable<SemVer> {
    init {
        require(major >= 0 && minor >= 0 && patch >= 0) {
            "SemVer components must be non-negative"
        }
    }

    override fun compareTo(other: SemVer): Int {
        major.compareTo(other.major).let { if (it != 0) return it }
        minor.compareTo(other.minor).let { if (it != 0) return it }
        return patch.compareTo(other.patch)
    }

    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        private val CORE_PATTERN = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)$")

        /**
         * Parses `vMAJOR.MINOR.PATCH` or `MAJOR.MINOR.PATCH`.
         * Returns null for malformed tags or prerelease/build variants.
         */
        fun parse(raw: String): SemVer? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return null
            val match = CORE_PATTERN.matchEntire(trimmed) ?: return null
            val major = match.groupValues[1].toIntOrNull() ?: return null
            val minor = match.groupValues[2].toIntOrNull() ?: return null
            val patch = match.groupValues[3].toIntOrNull() ?: return null
            return SemVer(major, minor, patch)
        }
    }
}
