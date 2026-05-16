package io.zlero.cRFramework.nms

import io.zlero.cRFramework.nms.version.NmsVersion

/**
 * NMS 서비스 진입점 — 버전 자동 감지 후 적합한 구현체 제공
 *
 * 사용법:
 *   NMSServiceManager.player.sendActionBar(player, "§a안녕!")
 *   NMSServiceManager.item.setString(item, "cr_id", "my_sword")
 *   NMSServiceManager.entity.setCustomName(entity, "§cBoss")
 *   NMSServiceManager.version  // NmsVersion.V1_20_4
 */
object NMSServiceManager {

    val version: NmsVersion get() = NmsVersion.current

    val player: NmsPlayerService by lazy {
        if (version.isAtLeast(NmsVersion.V1_20)) PlayerServiceModern()
        else PlayerServiceLegacy()
    }

    val item: NmsItemService by lazy {
        PersistentDataItemService()
    }

    val entity: NmsEntityService by lazy {
        EntityServiceImpl()
    }

    fun init() {
        val label = if (version == NmsVersion.UNKNOWN) "알 수 없음" else version.name
        println("[CRFramework/NMS] 서버 버전: ${NmsVersion.versionString} ($label)")
    }
}