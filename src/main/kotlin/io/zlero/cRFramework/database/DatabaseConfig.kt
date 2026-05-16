package io.zlero.cRFramework.database

enum class DatabaseType { SQLITE, MYSQL, H2 }

/**
 * 데이터베이스 연결 설정
 *
 * 기본값은 SQLite (별도 설정 불필요):
 *   DatabaseConfig()
 *
 * MySQL 사용:
 *   DatabaseConfig(
 *       type     = DatabaseType.MYSQL,
 *       host     = "localhost",
 *       port     = 3306,
 *       database = "myserver",
 *       username = "root",
 *       password = "1234"
 *   )
 */
data class DatabaseConfig(
    val type    : DatabaseType = DatabaseType.SQLITE,
    val fileName: String       = "data.db",
    val host    : String       = "localhost",
    val port    : Int          = 3306,
    val database: String       = "crframework",
    val username: String       = "root",
    val password: String       = "",
    val poolSize: Int          = 5
)