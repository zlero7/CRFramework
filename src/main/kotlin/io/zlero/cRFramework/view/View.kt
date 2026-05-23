package io.zlero.cRFramework.view

import io.zlero.cRFramework.view.scope.ButtonElement
import io.zlero.cRFramework.view.scope.CreateScope
import io.zlero.cRFramework.view.scope.RenderScope
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin

/**
 * GUI View 추상 클래스
 *
 * ─ 생명주기 ────────────────────────────────────────────────────
 *   onCreate()  : 버튼/레이아웃 정의 (열릴 때 1회)
 *   onRender()  : 인벤토리 아이템 배치 (열릴 때 + 리렌더 시마다)
 *   onClose()   : 정리 로직 (닫힐 때)
 *
 * ─ 반응형 ──────────────────────────────────────────────────────
 *   button(slot, state = vm.page) { ... } 처럼 State를 연결하면
 *   상태값이 바뀔 때마다 자동으로 리렌더링됨
 *
 * 사용법:
 *   class ShopView(plugin: JavaPlugin, val vm: ShopViewModel)
 *       : View(plugin, "§8상점", rows = 4) {
 *
 *       override fun CreateScope.onCreate() {
 *           button(13, state = vm.balance) {
 *               item { _ -> ItemStack(Material.DIAMOND) }
 *               onClick { p -> vm.balance.value -= 100 }
 *           }
 *           border(rows = 4) { item(Material.GRAY_STAINED_GLASS_PANE) }
 *       }
 *
 *       override fun RenderScope.onRender(player: Player) {
 *           slot(4) { ItemStack(Material.PAPER).also {
 *               it.itemMeta = it.itemMeta!!.also { m -> m.setDisplayName("§f${vm.balance.value}G") }
 *           }}
 *       }
 *
 *       override fun onClose(player: Player) {
 *           player.sendMessage("§7상점을 닫았습니다.")
 *       }
 *   }
 *
 *   // 열기
 *   ShopView(plugin, ShopViewModel()).open(player)
 */
abstract class View(
    protected val plugin: JavaPlugin,
    private val title: String,
    protected val rows: Int = 3
) {
    private var buttons  : List<ButtonElement> = emptyList()
    private var inventory: Inventory?          = null
    private var viewer   : Player?             = null

    // ─── 구현 ─────────────────────────────────────────────────
    protected abstract fun CreateScope.onCreate()
    protected open    fun RenderScope.onRender(player: Player) {}
    protected open    fun onClose(player: Player) {}

    // ─── 공개 API ─────────────────────────────────────────────

    fun open(player: Player) {
        // 1. 버튼 정의
        val scope = CreateScope()
        scope.onCreate()
        buttons = scope.buttons

        // 2. 인벤토리 생성
        val inv = Bukkit.createInventory(null, rows * 9, title)
        inventory = inv
        viewer    = player

        // 3. 렌더링
        renderInto(inv, player)

        // 4. 반응형 상태 구독 → 변경 시 자동 리렌더
        buttons.forEach { btn ->
            btn.state?.subscribe {
                Bukkit.getScheduler().runTask(plugin, Runnable { rerender() })
            }
        }

        // 5. 레지스트리 등록 후 오픈
        ViewRegistry.register(player, this)
        player.openInventory(inv)
    }

    fun rerender() {
        val inv = inventory ?: return
        val p   = viewer   ?: return
        renderInto(inv, p)
    }

    fun close() {
        val p = viewer ?: return
        onClose(p)
        ViewRegistry.unregister(p)
        inventory = null
        viewer    = null
        p.closeInventory()
    }

    internal fun handleClick(slot: Int) {
        val p = viewer ?: return
        buttons.findLast { it.slot == slot }?.clickHandler?.invoke(p)
    }

    internal fun handleClose() {
        val p = viewer ?: return
        onClose(p)
        ViewRegistry.unregister(p)
        inventory = null
        viewer    = null
    }

    // ─── 내부 ─────────────────────────────────────────────────
    private fun renderInto(inv: Inventory, player: Player) {
        buttons.forEach { btn -> inv.setItem(btn.slot, btn.itemProvider(player)) }
        RenderScope(player, inv).onRender(player)
    }
}

/** Player → View 1:1 레지스트리 (ViewListener에서 라우팅에 사용) */
internal object ViewRegistry {
    private val map = mutableMapOf<Player, View>()

    fun register(player: Player, view: View) { map[player] = view }
    fun unregister(player: Player)           { map.remove(player) }
    fun get(player: Player): View?           = map[player]
}