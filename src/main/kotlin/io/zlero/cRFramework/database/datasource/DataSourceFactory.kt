package io.zlero.cRFramework.database.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.zlero.cRFramework.database.DatabaseConfig
import io.zlero.cRFramework.database.DatabaseType
import org.jetbrains.exposed.sql.Database
import java.io.File

/**
 * DatabaseConfig를 읽어 Exposed Database 연결을 생성하는 팩토리
 * SQLite는 단순 JDBC, MySQL/H2는 HikariCP 커넥션 풀 사용
 */
object DataSourceFactory {

    private var dataSource: HikariDataSource? = null

    fun connect(config: DatabaseConfig, dataFolder: File): Database {
        return when (config.type) {
            DatabaseType.SQLITE -> connectSQLite(config, dataFolder)
            DatabaseType.MYSQL  -> connectPool(config, buildMySQLUrl(config))
            DatabaseType.H2     -> connectPool(config, buildH2Url(config, dataFolder))
        }
    }

    fun close() {
        dataSource?.close()
        dataSource = null
    }

    private fun connectSQLite(config: DatabaseConfig, dataFolder: File): Database {
        dataFolder.mkdirs()
        val path = File(dataFolder, config.fileName).absolutePath
        return Database.connect("jdbc:sqlite:$path", "org.sqlite.JDBC")
    }

    private fun connectPool(config: DatabaseConfig, url: String): Database {
        val hc = HikariConfig().apply {
            jdbcUrl            = url
            username           = config.username
            password           = config.password
            maximumPoolSize    = config.poolSize
            minimumIdle        = 1
            connectionTimeout  = 30_000
            idleTimeout        = 600_000
            maxLifetime        = 1_800_000
            connectionTestQuery = "SELECT 1"
        }
        dataSource = HikariDataSource(hc)
        return Database.connect(dataSource!!)
    }

    private fun buildMySQLUrl(c: DatabaseConfig) =
        "jdbc:mysql://${c.host}:${c.port}/${c.database}" +
                "?useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC"

    private fun buildH2Url(c: DatabaseConfig, dataFolder: File): String {
        dataFolder.mkdirs()
        val path = File(dataFolder, c.fileName).absolutePath
        return "jdbc:h2:$path;MODE=MySQL;DB_CLOSE_DELAY=-1"
    }
}