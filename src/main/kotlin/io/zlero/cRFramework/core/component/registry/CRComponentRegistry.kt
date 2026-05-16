package io.zlero.cRFramework.core.component.registry

import io.zlero.cRFramework.core.component.annotation.Setup
import io.zlero.cRFramework.core.component.annotation.Teardown
import org.bukkit.plugin.java.JavaPlugin
import kotlin.reflect.KClass

class CRComponentRegistry(private val plugin: JavaPlugin) {

    private val beans = LinkedHashMap<KClass<*>, Any>()

    fun <T : Any> register(type: KClass<T>, instance: T) {
        beans[type] = instance
    }

    @Suppress("UNCHECKED_CAST")
    fun registerAny(type: KClass<*>, instance: Any) {
        beans[type] = instance
    }

    fun scan(classes: List<KClass<*>>) {
        val remaining = classes.toMutableList()
        var lastSize = -1
        while (remaining.isNotEmpty() && remaining.size != lastSize) {
            lastSize = remaining.size
            val iter = remaining.iterator()
            while (iter.hasNext()) {
                val klass = iter.next()
                if (beans.containsKey(klass)) { iter.remove(); continue }
                if (tryCreate(klass)) iter.remove()
            }
        }
        if (remaining.isNotEmpty()) {
            error("[CRFramework] DI 해결 실패 (순환 의존성 또는 미등록 타입): ${remaining.map { it.simpleName }}")
        }

        classes.forEach { klass ->
            beans[klass]?.let { instance ->
                instance.javaClass.methods
                    .filter { it.isAnnotationPresent(Setup::class.java) }
                    .forEach { method -> runCatching { method.invoke(instance) } }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getBean(type: KClass<T>): T? {
        // 1. 정확한 타입 매칭
        (beans[type] as? T)?.let { return it }
        // 2. Java Class 기반 서브타입 매칭 (kotlin.reflect 없이)
        val javaType = type.java
        return beans.values.firstOrNull { javaType.isAssignableFrom(it.javaClass) } as? T
    }

    fun getAllBeans(): Collection<Any> = beans.values

    fun teardown() {
        beans.values.reversed().forEach { instance ->
            instance.javaClass.methods
                .filter { it.isAnnotationPresent(Teardown::class.java) }
                .forEach { method -> runCatching { method.invoke(instance) } }
        }
        beans.clear()
    }

    private fun tryCreate(klass: KClass<*>): Boolean {
        // kotlin.reflect(primaryConstructor) 대신 Java reflection 사용
        val javaCtor = klass.java.constructors
            .minByOrNull { it.parameterCount }  // 파라미터 가장 적은 생성자 우선
            ?: return false

        val args = mutableListOf<Any>()
        for (paramType in javaCtor.parameterTypes) {
            val dep = when {
                // JavaPlugin 서브타입이면 plugin 주입
                JavaPlugin::class.java.isAssignableFrom(paramType) -> plugin
                // 그 외엔 beans에서 타입 호환되는 인스턴스 탐색
                else -> beans.values.firstOrNull { paramType.isAssignableFrom(it.javaClass) }
                    ?: return false
            }
            args += dep
        }

        beans[klass] = javaCtor.newInstance(*args.toTypedArray())
        return true
    }
}