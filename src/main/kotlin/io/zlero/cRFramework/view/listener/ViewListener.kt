package io.zlero.cRFramework.view.listener

import io.zlero.cRFramework.view.ViewRegistry
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * 모든 인벤토리 이벤트를 가로채 ViewRegistry를 통해 해당 View로 라우팅
 * CRPlugin.onEnable()에서 자동 등록됨
 */
class ViewListener : Listener {

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        val view   = ViewRegistry.get(player) ?: return
        e.isCancelled = true
        if (e.rawSlot < 0 || e.rawSlot >= e.view.topInventory.size) return
        view.handleClick(e.rawSlot)
    }

    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        val player = e.player as? Player ?: return
        ViewRegistry.get(player)?.handleClose()
    }
}