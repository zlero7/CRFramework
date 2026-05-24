package io.zlero.cRFramework

import org.bukkit.plugin.java.JavaPlugin

class CRFrameworkPlugin : JavaPlugin() {
    override fun onEnable() {
        // ViewListener는 서버 전체에서 단 한 번만 등록 (CRPlugin 상속 플러그인이 여러 개여도 중복 등록 방지)
        server.pluginManager.registerEvents(io.zlero.cRFramework.view.listener.ViewListener(), this)
        logger.info("[CRFramework] CRFramework v${description.version} loaded.")
    }

    override fun onDisable() {
        logger.info("[CRFramework] CRFramework disabled.")
    }
}
