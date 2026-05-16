package io.zlero.cRFramework.database

import io.zlero.cRFramework.core.component.annotation.Module
import io.zlero.cRFramework.core.component.annotation.Setup
import io.zlero.cRFramework.core.component.annotation.Teardown
import io.zlero.cRFramework.database.datasource.DataSourceFactory
import io.zlero.cRFramework.database.repository.PlayerRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * DB 초기화 모듈 — components()에 추가하면 자동 실행
 *
 * 사용법:
 *   // CRPlugin.components()에 추가
 *   DatabaseModule::class,
 *
 *   // onCREnabled()에서 테이블/레포지토리 등록
 *   override fun onCREnabled() {
 *       val db = inject<DatabaseModule>()
 *       db.addTable(PlayerTable)
 *       db.addPlayerRepository(inject<MoneyRepository>())
 *   }
 */
@Module
class DatabaseModule(
    private val plugin: JavaPlugin,
    val config: DatabaseConfig = DatabaseConfig()
) {
    private val tables      = mutableListOf<org.jetbrains.exposed.sql.Table>()
    private val playerRepos = mutableListOf<PlayerRepository<*, *>>()

    fun addTable(vararg table: org.jetbrains.exposed.sql.Table) {
        tables += table
    }

    fun addPlayerRepository(vararg repo: PlayerRepository<*, *>) {
        playerRepos += repo
    }

    @Setup
    fun onSetup() {
        // 1. DB 연결
        DataSourceFactory.connect(config, plugin.dataFolder)
        plugin.logger.info("[CRFramework/DB] ${config.type} 연결 완료")

        // 2. 테이블 자동 생성
        if (tables.isNotEmpty()) {
            transaction {
                SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray())
            }
            plugin.logger.info("[CRFramework/DB] 테이블 ${tables.size}개 초기화 완료")
        }

        // 3. 플레이어 이벤트 자동 연결
        if (playerRepos.isNotEmpty()) {
            plugin.server.pluginManager.registerEvents(
                PlayerLifecycleListener(playerRepos), plugin
            )
            plugin.logger.info("[CRFramework/DB] PlayerRepository ${playerRepos.size}개 등록")
        }
    }

    @Teardown
    fun onTeardown() {
        playerRepos.forEach { it.saveAll() }
        DataSourceFactory.close()
        plugin.logger.info("[CRFramework/DB] 데이터베이스 연결 종료")
    }
}

private class PlayerLifecycleListener(
    private val repos: List<PlayerRepository<*, *>>
) : Listener {

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) = repos.forEach { it.onJoin(e.player.uniqueId) }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) = repos.forEach { it.onQuit(e.player.uniqueId) }
}