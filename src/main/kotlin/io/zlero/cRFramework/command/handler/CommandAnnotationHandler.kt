package io.zlero.cRFramework.command.handler

import io.zlero.cRFramework.command.CommandContext
import io.zlero.cRFramework.command.CommandException
import io.zlero.cRFramework.command.annotation.Command
import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class CommandAnnotationHandler(private val plugin: JavaPlugin) {

    private val commandMap: CommandMap by lazy {
        Bukkit.getServer().javaClass
            .getDeclaredField("commandMap")
            .also { it.isAccessible = true }
            .get(Bukkit.getServer()) as CommandMap
    }

    private val pluginCommandCtor by lazy {
        PluginCommand::class.java
            .getDeclaredConstructor(String::class.java, Plugin::class.java)
            .also { it.isAccessible = true }
    }

    fun register(instance: Any) {
        instance.javaClass.methods
            .mapNotNull { method ->
                method.getAnnotation(Command::class.java)?.let { method to it }
            }
            .forEach { (method, ann) ->
                val cmd = pluginCommandCtor.newInstance(ann.name, plugin).apply {
                    description = ann.description
                    if (ann.permission.isNotEmpty()) permission = ann.permission
                    if (ann.aliases.isNotEmpty()) aliases = ann.aliases.toList()
                    if (ann.usage.isNotEmpty()) usage = ann.usage

                    setExecutor { sender, _, label, args ->
                        try {
                            method.invoke(instance, CommandContext(sender, args, label))
                        } catch (e: CommandException) {
                            sender.sendMessage("§c${e.message}")
                        } catch (e: Exception) {
                            sender.sendMessage("§c명령어 처리 중 오류가 발생했습니다.")
                            plugin.logger.warning("[CRFramework] 명령어 /${ann.name} 오류: ${e.message}")
                        }
                        true
                    }
                }
                commandMap.register(plugin.name.lowercase(), cmd)
                plugin.logger.info("[CRFramework] 명령어 /${ann.name} 등록")
            }
    }
}