package io.zlero.cRFramework.view.listener

import io.zlero.cRFramework.view.ViewRegistry
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 모든 인벤토리 이벤트를 가로채 ViewRegistry를 통해 해당 View로 라우팅
 * CRPlugin.onEnable()에서 자동 등록됨
 *
 * 중복 클릭 방지:
 *   - DOUBLE_CLICK 이벤트 무시 (빠른 더블클릭 시 LEFT+LEFT+DOUBLE_CLICK 세 이벤트 발생)
 *   - 100ms 시간 기반 디바운스 (같은 플레이어가 동일 슬롯을 100ms 내 재클릭 무시)
 */
class ViewListener : Listener {

    /** uuid → (slot → lastClickMs) */
    private val lastClick = ConcurrentHashMap<UUID, Long>()

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        val view   = ViewRegistry.get(player) ?: return
        e.isCancelled = true
        if (e.rawSlot < 0 || e.rawSlot >= e.view.topInventory.size) return
        // 더블클릭 이벤트는 무시 (빠른 클릭 시 LEFT+LEFT+DOUBLE_CLICK 3회 발생 방지)
        if (e.click == ClickType.DOUBLE_CLICK) return
        // 100ms 디바운스: 같은 플레이어의 연속 이벤트 중복 방지
        val now  = System.currentTimeMillis()
        val last = lastClick[player.uniqueId] ?: 0L
        if (now - last < 100L) return
        lastClick[player.uniqueId] = now

        val isRight = e.click == ClickType.RIGHT || e.click == ClickType.SHIFT_RIGHT
        view.handleClick(e.rawSlot, isRight)
    }

    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        val player = e.player as? Player ?: return
        lastClick.remove(player.uniqueId)
        ViewRegistry.get(player)?.handleClose()
    }
}