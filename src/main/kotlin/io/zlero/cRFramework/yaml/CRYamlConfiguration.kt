package io.zlero.cRFramework.yaml

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * 타입 세이프 YAML 설정 파일 기반 클래스
 *
 * - 파일이 없으면 resources 에서 자동 복사 (없으면 빈 파일 생성)
 * - reload() 로 파일 재로드 가능
 * - &색상코드 자동 변환 지원
 * - Location 직렬화/역직렬화 지원
 *
 * 사용법:
 *   @Configuration("config.yml")
 *   class MainConfig(plugin: JavaPlugin) : CRYamlConfiguration(plugin, "config.yml") {
 *       val prefix      get() = colorString("prefix",              "&8[&bServer&8] &r")
 *       val maxNickLen  get() = int("nickname.max-length",         16)
 *       val allowColor  get() = boolean("nickname.allow-color",    true)
 *       val blacklist   get() = stringList("nickname.blacklist")
 *       val spawnLoc    get() = getLocation("spawn")
 *   }
 */
abstract class CRYamlConfiguration(
    private val plugin: JavaPlugin,
    private val fileName: String
) {
    private val file: File get() = File(plugin.dataFolder, fileName)
    private var _config: YamlConfiguration = loadOrCreate()

    protected val config: YamlConfiguration get() = _config

    private fun loadOrCreate(): YamlConfiguration {
        plugin.dataFolder.mkdirs()
        if (!file.exists()) {
            if (plugin.getResource(fileName) != null) plugin.saveResource(fileName, false)
            else file.createNewFile()
        }
        return YamlConfiguration.loadConfiguration(file)
    }

    fun reload() { _config = YamlConfiguration.loadConfiguration(file) }
    fun save()   { _config.save(file) }
    fun set(path: String, value: Any?) { _config.set(path, value); save() }

    // ─── 기본 타입 ────────────────────────────────────────────
    protected fun string(path: String, default: String = "")        = _config.getString(path, default) ?: default
    protected fun int(path: String, default: Int = 0)               = _config.getInt(path, default)
    protected fun long(path: String, default: Long = 0L)            = _config.getLong(path, default)
    protected fun double(path: String, default: Double = 0.0)       = _config.getDouble(path, default)
    protected fun boolean(path: String, default: Boolean = false)   = _config.getBoolean(path, default)
    protected fun stringList(path: String): List<String>            = _config.getStringList(path)
    protected fun intList(path: String): List<Int>                  = _config.getIntegerList(path)
    protected fun keys(path: String): Set<String>                   = _config.getConfigurationSection(path)?.getKeys(false) ?: emptySet()

    // ─── Nullable ─────────────────────────────────────────────
    protected fun stringOrNull(path: String)  = _config.getString(path)
    protected fun intOrNull(path: String)     = if (_config.contains(path)) _config.getInt(path) else null
    protected fun longOrNull(path: String)    = if (_config.contains(path)) _config.getLong(path) else null
    protected fun doubleOrNull(path: String)  = if (_config.contains(path)) _config.getDouble(path) else null
    protected fun booleanOrNull(path: String) = if (_config.contains(path)) _config.getBoolean(path) else null

    // ─── 색상 변환 (&코드 → §코드) ───────────────────────────
    protected fun colorString(path: String, default: String = ""): String =
        ChatColor.translateAlternateColorCodes('&', string(path, default))

    protected fun colorStringList(path: String): List<String> =
        stringList(path).map { ChatColor.translateAlternateColorCodes('&', it) }

    // ─── Location 직렬화 ──────────────────────────────────────
    protected fun setLocation(path: String, loc: Location) {
        _config.set("$path.world", loc.world?.name)
        _config.set("$path.x",     loc.x)
        _config.set("$path.y",     loc.y)
        _config.set("$path.z",     loc.z)
        _config.set("$path.yaw",   loc.yaw.toDouble())
        _config.set("$path.pitch", loc.pitch.toDouble())
        save()
    }

    protected fun getLocation(path: String): Location? {
        val worldName = _config.getString("$path.world") ?: return null
        val world     = Bukkit.getWorld(worldName) ?: return null
        return Location(
            world,
            _config.getDouble("$path.x"),
            _config.getDouble("$path.y"),
            _config.getDouble("$path.z"),
            _config.getDouble("$path.yaw").toFloat(),
            _config.getDouble("$path.pitch").toFloat()
        )
    }
}