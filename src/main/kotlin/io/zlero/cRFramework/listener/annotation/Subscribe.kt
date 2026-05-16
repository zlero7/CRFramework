package io.zlero.cRFramework.listener.annotation

import org.bukkit.event.EventPriority

/**
 * 함수에 붙이면 이벤트 리스너로 자동 등록
 *
 * 사용법:
 *   @Subscribe
 *   fun onJoin(e: PlayerJoinEvent) { ... }
 *
 *   @Subscribe(priority = EventPriority.HIGH, ignoreCancelled = true)
 *   fun onDamage(e: EntityDamageEvent) { ... }
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Subscribe(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false
)