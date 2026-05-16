package io.zlero.cRFramework.nms.version

import org.bukkit.Bukkit

/**
 * 서버 버전 감지 및 비교
 *
 * 사용법:
 *   NmsVersion.current                              // V1_20_4
 *   NmsVersion.current.isAtLeast(NmsVersion.V1_21)  // true
 *   NmsVersion.current.isBefore(NmsVersion.V1_21)   // false
 */
enum class NmsVersion(val packed: Int) {
    V1_17  (11700),
    V1_18  (11800),
    V1_19  (11900),
    V1_20  (12000),
    V1_20_4(12004),
    V1_20_6(12006),
    V1_21  (12100),
    V1_21_1(12101),
    V1_21_3(12103),
    V1_21_4(12104),
    V1_21_5(12105),
    V1_21_7(12107),
    UNKNOWN(0);

    fun isAtLeast(version: NmsVersion): Boolean = this.packed >= version.packed
    fun isBefore(version: NmsVersion): Boolean  = this.packed < version.packed

    companion object {
        val current: NmsVersion by lazy { detect() }

        val versionString: String by lazy {
            Bukkit.getBukkitVersion().split("-").first()
        }

        private fun detect(): NmsVersion {
            val parts  = versionString.split(".")
            val major  = parts.getOrNull(0)?.toIntOrNull() ?: return UNKNOWN
            val minor  = parts.getOrNull(1)?.toIntOrNull() ?: return UNKNOWN
            val patch  = parts.getOrNull(2)?.toIntOrNull() ?: 0
            val packed = major * 10000 + minor * 100 + patch
            return values()
                .filter { it != UNKNOWN && it.packed <= packed }
                .maxByOrNull { it.packed }
                ?: UNKNOWN
        }
    }
}