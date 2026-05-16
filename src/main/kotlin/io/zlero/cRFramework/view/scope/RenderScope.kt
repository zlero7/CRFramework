package io.zlero.cRFramework.view.scope

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * View.onRender() 안에서 인벤토리에 아이템을 배치하는 스코프
 *
 * 사용법:
 *   override fun RenderScope.onRender(player: Player) {
 *       slot(4)  { ItemStack(Material.PAPER) }
 *       slots(listOf(10, 11, 12)) { ItemStack(Material.DIAMOND) }
 *       clear(0) // 특정 슬롯 비우기
 *   }
 */
class RenderScope(val player: Player, val inventory: Inventory) {

    fun slot(index: Int, provider: () -> ItemStack?) {
        inventory.setItem(index, provider())
    }

    fun slots(indices: Iterable<Int>, provider: () -> ItemStack?) =
        indices.forEach { inventory.setItem(it, provider()) }

    fun clear()            = inventory.clear()
    fun clear(index: Int)  = inventory.setItem(index, null)
}