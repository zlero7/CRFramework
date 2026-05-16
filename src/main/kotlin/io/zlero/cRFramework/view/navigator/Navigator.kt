package io.zlero.cRFramework.view.navigator

import io.zlero.cRFramework.view.View
import org.bukkit.entity.Player

/**
 * View 간 이동을 스택 구조로 관리
 *
 * 사용법:
 *   val nav = Navigator(player)
 *   nav.open(MainMenuView(plugin))   // 스택 초기화 후 열기
 *   nav.push(ShopView(plugin, vm))   // 현재 위에 쌓기
 *   nav.goBack()                     // 이전 View로 돌아가기
 *   nav.close()                      // 전체 닫기
 */
class Navigator(private val player: Player) {
    private val stack = ArrayDeque<View>()

    /** 스택 초기화 후 새 View 열기 */
    fun open(view: View) {
        stack.clear()
        stack.addLast(view)
        view.open(player)
    }

    /** 현재 View 위에 새 View 쌓기 */
    fun push(view: View) {
        stack.addLast(view)
        view.open(player)
    }

    /** 이전 View로 돌아가기 */
    fun goBack() {
        if (stack.size <= 1) { close(); return }
        stack.removeLast()
        stack.last().open(player)
    }

    /** 전체 닫기 */
    fun close() {
        stack.forEach { it.handleClose() }
        stack.clear()
        player.closeInventory()
    }

    val current: View?      get() = stack.lastOrNull()
    val canGoBack: Boolean  get() = stack.size > 1
    val depth: Int          get() = stack.size
}