package io.zlero.cRFramework.scheduler

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import kotlin.coroutines.CoroutineContext

// ─────────────────────────────────────────────────────────────────────────────
// Bukkit 코루틴 디스패처
// ─────────────────────────────────────────────────────────────────────────────

/** 코루틴을 Bukkit 메인 스레드에서 실행 */
class BukkitMainDispatcher(private val plugin: JavaPlugin) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) block.run()
        else Bukkit.getScheduler().runTask(plugin, block)
    }
}

/** 코루틴을 Bukkit 비동기 스레드에서 실행 */
class BukkitAsyncDispatcher(private val plugin: JavaPlugin) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, block)
    }
}

/** 플러그인 생명주기에 연결된 코루틴 스코프 생성 */
fun JavaPlugin.pluginScope(): CoroutineScope =
    CoroutineScope(SupervisorJob() + BukkitMainDispatcher(this) + CoroutineName(name))

/** 메인 스레드로 전환 후 실행 */
suspend fun <T> JavaPlugin.withMain(block: suspend () -> T): T =
    withContext(BukkitMainDispatcher(this)) { block() }

/** 비동기 스레드로 전환 후 실행 */
suspend fun <T> JavaPlugin.withAsync(block: suspend () -> T): T =
    withContext(BukkitAsyncDispatcher(this)) { block() }

// ─────────────────────────────────────────────────────────────────────────────
// CRScheduler
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Bukkit 스케줄러 간편 래퍼
 *
 * 사용법:
 *   val s = CRScheduler(plugin)
 *   s.runLater(60L) { ... }            // 3초 후 메인 스레드
 *   s.runEverySeconds(5L) { ... }      // 5초마다 반복
 *   s.runTimes(3, 20L) { i -> ... }    // 3번 반복 후 자동 취소
 *   s.async({ loadDB() }) { result ->  // 비동기 후 메인 콜백
 *       player.sendMessage(result)
 *   }
 */
class CRScheduler(private val plugin: JavaPlugin) {

    // ─── 메인 스레드 ──────────────────────────────────────────
    fun run(block: () -> Unit): BukkitTask =
        Bukkit.getScheduler().runTask(plugin, Runnable(block))

    fun runLater(delayTicks: Long, block: () -> Unit): BukkitTask =
        Bukkit.getScheduler().runTaskLater(plugin, Runnable(block), delayTicks)

    fun runTimer(delayTicks: Long, periodTicks: Long, block: () -> Unit): BukkitTask =
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable(block), delayTicks, periodTicks)

    // ─── 비동기 ───────────────────────────────────────────────
    fun runAsync(block: () -> Unit): BukkitTask =
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable(block))

    fun runLaterAsync(delayTicks: Long, block: () -> Unit): BukkitTask =
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable(block), delayTicks)

    fun runTimerAsync(delayTicks: Long, periodTicks: Long, block: () -> Unit): BukkitTask =
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable(block), delayTicks, periodTicks)

    // ─── 초 단위 편의 함수 ────────────────────────────────────
    fun runAfterSeconds(sec: Long, block: () -> Unit)      = runLater(sec * 20, block)
    fun runAfterSecondsAsync(sec: Long, block: () -> Unit) = runLaterAsync(sec * 20, block)
    fun runEverySeconds(sec: Long, block: () -> Unit)      = runTimer(0, sec * 20, block)
    fun runEverySecondsAsync(sec: Long, block: () -> Unit) = runTimerAsync(0, sec * 20, block)

    /**
     * n번 실행 후 자동 취소
     * block(remaining) — remaining: 남은 횟수 (n → 1)
     */
    fun runTimes(times: Int, periodTicks: Long, block: (remaining: Int) -> Unit) {
        var count = 0
        var task: BukkitTask? = null
        task = runTimer(0, periodTicks) {
            count++
            block(times - count + 1)
            if (count >= times) task?.cancel()
        }
    }

    /**
     * 비동기 작업 → 메인 스레드 콜백
     *
     * 사용법:
     *   scheduler.async(
     *       task = { loadFromDB() },
     *       then = { result -> player.sendMessage(result) }
     *   )
     */
    fun <T> async(task: () -> T, then: (T) -> Unit) {
        runAsync {
            val result = task()
            run { then(result) }
        }
    }
}