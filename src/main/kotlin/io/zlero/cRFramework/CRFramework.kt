package io.zlero.cRFramework

import io.zlero.cRFramework.command.handler.CommandAnnotationHandler
import io.zlero.cRFramework.core.component.registry.CRComponentRegistry
import io.zlero.cRFramework.listener.handler.ListenerAnnotationHandler
import io.zlero.cRFramework.nms.NMSServiceManager
import io.zlero.cRFramework.scheduler.CRScheduler
import org.bukkit.plugin.java.JavaPlugin
import kotlin.reflect.KClass

/**
 * CRFramework 베이스 플러그인
 *
 * 사용법:
 *   class MyPlugin : CRPlugin() {
 *       override fun components() = listOf(
 *           MainConfig::class,
 *           DatabaseModule::class,
 *           MoneyRepository::class,
 *           NickService::class,
 *           NickCommand::class,
 *           PlayerListener::class,
 *       )
 *       override fun onCREnabled() {
 *           val db = inject<DatabaseModule>()
 *           db.addTable(MoneyTable)
 *           db.addPlayerRepository(inject<MoneyRepository>())
 *       }
 *   }
 */
abstract class CRPlugin : JavaPlugin() {

    lateinit var registry       : CRComponentRegistry      private set
    lateinit var commandHandler : CommandAnnotationHandler  private set
    lateinit var listenerHandler: ListenerAnnotationHandler private set

    val scheduler: CRScheduler by lazy { CRScheduler(this) }
    val nms get() = NMSServiceManager

    // ─── 구현 필수 ────────────────────────────────────────
    abstract fun components(): List<KClass<*>>

    // ─── 선택 오버라이드 ──────────────────────────────────
    protected open fun onCREnabled()  {}
    protected open fun onCRDisabled() {}

    // ─── 생명주기 ─────────────────────────────────────────
    final override fun onEnable() {
        // 1. NMS 버전 감지 및 출력
        NMSServiceManager.init()

        // 2. 매니저 초기화
        registry        = CRComponentRegistry(this)
        commandHandler  = CommandAnnotationHandler(this)
        listenerHandler = ListenerAnnotationHandler(this)

        // 4. 플러그인 자신을 빈으로 등록
        registry.registerAny(this::class, this)

        // 5. DI 스캔 (@Setup 자동 호출 포함)
        registry.scan(components())

        // 6. @Command / @Subscribe 자동 등록
        components().forEach { klass ->
            registry.getBean(klass)?.let { instance ->
                commandHandler.register(instance)
                listenerHandler.register(instance)
            }
        }

        // 7. 사용자 초기화 훅
        onCREnabled()

        logger.info("[CRFramework] §a${description.name} v${description.version} 활성화 ✓")
    }

    final override fun onDisable() {
        onCRDisabled()
        registry.teardown()
        listenerHandler.unregisterAll()
        logger.info("[CRFramework] ${description.name} 비활성화")
    }

    inline fun <reified T : Any> inject(): T =
        registry.getBean(T::class) ?: error("빈을 찾을 수 없습니다: ${T::class.simpleName}")
}