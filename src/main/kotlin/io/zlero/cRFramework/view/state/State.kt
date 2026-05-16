package io.zlero.cRFramework.view.state

/**
 * 반응형 상태 — 값이 바뀌면 구독자에게 자동 알림
 *
 * 사용법:
 *   val count = mutableStateOf(0)
 *   count.subscribe { v -> println("변경됨: $v") }
 *   count.value++   // 구독자 자동 호출
 */
interface State<T> {
    val value: T
    fun subscribe(observer: (T) -> Unit)
}

interface MutableState<T> : State<T> {
    override var value: T
    fun unsubscribeAll()
}

class MutableStateImpl<T>(initial: T) : MutableState<T> {
    private val observers = mutableListOf<(T) -> Unit>()

    override var value: T = initial
        set(new) {
            field = new
            observers.toList().forEach { it(new) }
        }

    override fun subscribe(observer: (T) -> Unit) { observers += observer }
    override fun unsubscribeAll() { observers.clear() }
}

fun <T> mutableStateOf(initial: T): MutableState<T> = MutableStateImpl(initial)