package io.zlero.cRFramework.database.repository

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 플레이어 UUID 기반 데이터 관리 + 인메모리 캐시
 *
 * 흐름:
 *   onJoin  → load() or createDefault() → cache 저장
 *   get()   → cache 조회 (O(1))
 *   update()→ cache 수정 + dirty 마킹
 *   onQuit  → dirty 항목만 save() → cache 제거
 *   saveAll → 서버 종료 시 전체 flush
 *
 * 사용법:
 *   @Repository
 *   class MoneyRepository : PlayerRepository<MoneyData, MoneyTable>(MoneyTable) {
 *       override fun load(uuid: UUID): MoneyData? = query {
 *           MoneyTable.selectAll()
 *               .where { MoneyTable.uuid eq uuid.toString() }
 *               .firstOrNull()
 *               ?.let { MoneyData(it[MoneyTable.money]) }
 *       }
 *       override fun save(uuid: UUID, data: MoneyData): Unit = query {
 *           // upsert 로직
 *       }
 *       override fun createDefault(uuid: UUID) = MoneyData(money = 0)
 *   }
 */
abstract class PlayerRepository<D : Any, T : IntIdTable>(
    protected val table: T
) {
    private data class Entry<D>(val data: D, var dirty: Boolean = false)

    private val cache = ConcurrentHashMap<UUID, Entry<D>>()

    // ─── 구현 필수 ──────────────────────────────────────
    abstract fun load(uuid: UUID): D?
    abstract fun save(uuid: UUID, data: D)
    open fun createDefault(uuid: UUID): D =
        error("${this::class.simpleName}.createDefault()를 구현하세요.")

    // ─── 생명주기 (DatabaseModule이 자동 호출) ──────────
    fun onJoin(uuid: UUID) {
        val data = load(uuid) ?: createDefault(uuid)
        cache[uuid] = Entry(data)
    }

    fun onQuit(uuid: UUID) {
        val entry = cache.remove(uuid) ?: return
        if (entry.dirty) save(uuid, entry.data)
    }

    fun saveAll() {
        cache.forEach { (uuid, entry) ->
            if (entry.dirty) runCatching { save(uuid, entry.data) }
        }
        cache.clear()
    }

    // ─── 공개 API ───────────────────────────────────────
    /** 캐시에서 데이터 조회 */
    fun get(uuid: UUID): D? = cache[uuid]?.data

    /** 캐시 데이터 수정 후 dirty 마킹 */
    fun update(uuid: UUID, block: D.() -> Unit): Boolean {
        val entry = cache[uuid] ?: return false
        entry.data.apply(block)
        entry.dirty = true
        return true
    }

    /** 즉시 DB 저장 (dirty 무관) */
    fun flush(uuid: UUID) {
        val entry = cache[uuid] ?: return
        save(uuid, entry.data)
        entry.dirty = false
    }

    fun isOnline(uuid: UUID): Boolean = cache.containsKey(uuid)

    protected fun <R> query(block: Transaction.() -> R): R = transaction { block() }
}