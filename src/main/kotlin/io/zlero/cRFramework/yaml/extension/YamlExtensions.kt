package io.zlero.cRFramework.yaml.extension

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

/**
 * ConfigurationSection 편의 확장 함수 모음
 */

// ─── 색상 변환 ────────────────────────────────────────────────
fun ConfigurationSection.colorString(path: String, default: String = ""): String =
    ChatColor.translateAlternateColorCodes('&', getString(path, default) ?: default)

fun ConfigurationSection.colorStringList(path: String): List<String> =
    getStringList(path).map { ChatColor.translateAlternateColorCodes('&', it) }

// ─── Nullable 조회 ────────────────────────────────────────────
fun ConfigurationSection.getStringOrNull(path: String): String? = getString(path)
fun ConfigurationSection.getIntOrNull(path: String): Int?       = if (contains(path)) getInt(path) else null
fun ConfigurationSection.getLongOrNull(path: String): Long?     = if (contains(path)) getLong(path) else null
fun ConfigurationSection.getDoubleOrNull(path: String): Double? = if (contains(path)) getDouble(path) else null
fun ConfigurationSection.getBoolOrNull(path: String): Boolean?  = if (contains(path)) getBoolean(path) else null

// ─── Location 직렬화 ──────────────────────────────────────────
fun ConfigurationSection.setLocation(path: String, loc: Location) {
    set("$path.world", loc.world?.name)
    set("$path.x",     loc.x)
    set("$path.y",     loc.y)
    set("$path.z",     loc.z)
    set("$path.yaw",   loc.yaw.toDouble())
    set("$path.pitch", loc.pitch.toDouble())
}

fun ConfigurationSection.getLocation(path: String): Location? {
    val worldName = getString("$path.world") ?: return null
    val world     = Bukkit.getWorld(worldName) ?: return null
    return Location(
        world,
        getDouble("$path.x"),
        getDouble("$path.y"),
        getDouble("$path.z"),
        getDouble("$path.yaw").toFloat(),
        getDouble("$path.pitch").toFloat()
    )
}