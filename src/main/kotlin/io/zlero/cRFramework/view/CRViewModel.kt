package io.zlero.cRFramework.view

import io.zlero.cRFramework.view.state.MutableState
import io.zlero.cRFramework.view.state.mutableStateOf

/**
 * View의 상태를 보관하는 ViewModel
 *
 * 사용법:
 *   class ShopViewModel : CRViewModel() {
 *       val page    = state(0)
 *       val balance = state(0L)
 *   }
 *
 *   // View에서
 *   val vm = ShopViewModel()
 *   vm.page.value++   // 상태 변경 → View 자동 리렌더링
 */
abstract class CRViewModel {
    private val states = mutableListOf<MutableState<*>>()

    protected fun <T> state(initial: T): MutableState<T> =
        mutableStateOf(initial).also { states += it }

    /** View 종료 시 모든 상태 구독 해제 */
    internal fun dispose() = states.forEach { it.unsubscribeAll() }

    open fun onOpen()  {}
    open fun onClose() {}
}