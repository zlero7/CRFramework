package io.zlero.cRFramework.view.scope

import io.zlero.cRFramework.view.state.State
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * GUI 슬롯 하나를 표현하는 요소
 */
data class ButtonElement(
    val slot         : Int,
    val itemProvider : (Player) -> ItemStack,
    val clickHandler : (Player) -> Unit,
    val state        : State<*>?
)

/**
 * View.onCreate() 안에서 버튼/레이아웃을 DSL로 정의하는 스코프
 *
 * 사용법:
 *   override fun CreateScope.onCreate() {
 *       button(13) {
 *           item { Material.DIAMOND_SWORD }
 *           onClick { player -> player.sendMessage("클릭!") }
 *       }
 *       border(rows = 3) { item(Material.GRAY_STAINED_GLASS_PANE) }
 *   }
 */
class CreateScope {
    internal val buttons = mutableListOf<ButtonElement>()

    fun button(slot: Int, state: State<*>? = null, init: ButtonBuilder.() -> Unit) {
        buttons += ButtonBuilder(slot, state).apply(init).build()
    }

    fun buttons(slots: Iterable<Int>, state: State<*>? = null, init: ButtonBuilder.() -> Unit) =
        slots.forEach { button(it, state, init) }

    /** rows × 9 크기의 인벤토리 테두리 슬롯 전체에 버튼 등록 */
    fun border(rows: Int, state: State<*>? = null, init: ButtonBuilder.() -> Unit) {
        val size = rows * 9
        val slots = (0 until 9) +
                (size - 9 until size) +
                (9 until size - 9).filter { it % 9 == 0 || it % 9 == 8 }
        buttons(slots, state, init)
    }

    /** 인벤토리 전체 슬롯 채우기 */
    fun fill(rows: Int, state: State<*>? = null, init: ButtonBuilder.() -> Unit) =
        buttons(0 until rows * 9, state, init)
}

/**
 * 버튼 DSL 빌더
 */
class ButtonBuilder(private val slot: Int, private val state: State<*>?) {
    private var itemProvider: (Player) -> ItemStack = { ItemStack(Material.AIR) }
    private var clickHandler: (Player) -> Unit      = {}

    fun item(provider: (Player) -> ItemStack) { itemProvider = provider }
    fun item(stack: ItemStack)                { itemProvider = { stack } }
    fun item(material: Material)              { itemProvider = { ItemStack(material) } }
    fun onClick(handler: (Player) -> Unit)    { clickHandler = handler }

    fun build() = ButtonElement(slot, itemProvider, clickHandler, state)
}