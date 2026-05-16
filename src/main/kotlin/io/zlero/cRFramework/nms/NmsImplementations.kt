package io.zlero.cRFramework.nms

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.time.Duration

// ─── 1.20+ Adventure API ────────────────────────────────────────────────────
internal class PlayerServiceModern : NmsPlayerService {

    override fun setTabName(player: Player, name: String) {
        player.playerListName(Component.text(name))
    }

    override fun setNameTag(player: Player, prefix: String, suffix: String) {
        val sb     = player.server.scoreboardManager.mainScoreboard
        val teamId = "cr_${player.uniqueId.toString().take(12)}"
        val team   = sb.getTeam(teamId) ?: sb.registerNewTeam(teamId)
        team.addEntry(player.name)
        team.prefix(Component.text(prefix))
        team.suffix(Component.text(suffix))
    }

    override fun clearNameTag(player: Player) {
        val sb     = player.server.scoreboardManager.mainScoreboard
        val teamId = "cr_${player.uniqueId.toString().take(12)}"
        sb.getTeam(teamId)?.unregister()
    }

    override fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        player.showTitle(Title.title(
            Component.text(title),
            Component.text(subtitle),
            Title.Times.times(
                Duration.ofMillis(fadeIn  * 50L),
                Duration.ofMillis(stay    * 50L),
                Duration.ofMillis(fadeOut * 50L)
            )
        ))
    }

    override fun sendActionBar(player: Player, message: String) {
        player.sendActionBar(Component.text(message))
    }

    override fun sendPacketTitle(player: Player, title: String) {
        player.showTitle(Title.title(Component.text(title), Component.empty()))
    }

    override fun sendPacketSubtitle(player: Player, subtitle: String) {
        player.showTitle(Title.title(Component.empty(), Component.text(subtitle)))
    }
}

// ─── 1.17~1.19 레거시 ───────────────────────────────────────────────────────
@Suppress("DEPRECATION")
internal class PlayerServiceLegacy : NmsPlayerService {

    override fun setTabName(player: Player, name: String) {
        player.setPlayerListName(name)
    }

    override fun setNameTag(player: Player, prefix: String, suffix: String) {
        val sb     = player.server.scoreboardManager.mainScoreboard
        val teamId = "cr_${player.name.take(12)}"
        val team   = sb.getTeam(teamId) ?: sb.registerNewTeam(teamId)
        team.addEntry(player.name)
        team.prefix = prefix
        team.suffix = suffix
    }

    override fun clearNameTag(player: Player) {
        player.server.scoreboardManager.mainScoreboard
            .getTeam("cr_${player.name.take(12)}")?.unregister()
    }

    override fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut)
    }

    override fun sendActionBar(player: Player, message: String) {
        player.spigot().sendMessage(
            net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
            net.md_5.bungee.api.chat.TextComponent(message)
        )
    }

    override fun sendPacketTitle(player: Player, title: String)       = sendTitle(player, title, "", 10, 70, 20)
    override fun sendPacketSubtitle(player: Player, subtitle: String) = sendTitle(player, "", subtitle, 10, 70, 20)
}

// ─── 엔티티 서비스 ────────────────────────────────────────────────────────────
internal class EntityServiceImpl : NmsEntityService {

    override fun setCustomName(entity: Entity, name: String?) {
        entity.customName(name?.let { Component.text(it) })
        entity.isCustomNameVisible = name != null
    }

    override fun setInvulnerable(entity: LivingEntity, value: Boolean) {
        entity.isInvulnerable = value
    }
}