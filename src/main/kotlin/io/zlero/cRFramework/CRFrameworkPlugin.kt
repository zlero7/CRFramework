package io.zlero.cRFramework

import org.bukkit.plugin.java.JavaPlugin

class CRFrameworkPlugin : JavaPlugin() {
    override fun onEnable() {
        logger.info("[CRFramework] CRFramework v${description.version} loaded.")
    }

    override fun onDisable() {
        logger.info("[CRFramework] CRFramework disabled.")
    }
}
