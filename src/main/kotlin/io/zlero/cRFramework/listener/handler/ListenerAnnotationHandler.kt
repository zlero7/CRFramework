package io.zlero.cRFramework.listener.handler

import io.zlero.cRFramework.listener.annotation.Subscribe
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class ListenerAnnotationHandler(private val plugin: JavaPlugin) {

    private val listeners = mutableListOf<Listener>()

    fun register(instance: Any) {
        val methods = instance.javaClass.methods.mapNotNull { method ->
            val sub = method.getAnnotation(Subscribe::class.java) ?: return@mapNotNull null
            val eventClass = method.parameterTypes.firstOrNull() ?: return@mapNotNull null
            if (!Event::class.java.isAssignableFrom(eventClass)) return@mapNotNull null
            @Suppress("UNCHECKED_CAST")
            Triple(method, sub, eventClass as Class<out Event>)
        }
        if (methods.isEmpty()) return

        val dummy = object : Listener {}
        listeners += dummy

        methods.forEach { (method, sub, eventClass) ->
            plugin.server.pluginManager.registerEvent(
                eventClass, dummy, sub.priority,
                { _, event ->
                    if (eventClass.isInstance(event))
                        runCatching { method.invoke(instance, event) }
                            .onFailure { ex ->
                                val cause = (ex as? java.lang.reflect.InvocationTargetException)?.cause ?: ex
                                plugin.logger.warning("[CRFramework] 리스너 오류 [${method.name}]: ${cause.message}")
                                cause.stackTrace.take(8).forEach { plugin.logger.warning("  at $it") }
                            }
                },
                plugin,
                sub.ignoreCancelled
            )
        }
        plugin.logger.info("[CRFramework] ${instance.javaClass.simpleName} 리스너 등록 (${methods.size}개)")
    }

    fun unregisterAll() {
        listeners.forEach { HandlerList.unregisterAll(it) }
        listeners.clear()
    }
}