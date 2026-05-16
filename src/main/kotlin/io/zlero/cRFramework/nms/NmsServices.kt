package io.zlero.cRFramework.nms

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface NmsPlayerService {
    fun setTabName(player: Player, name: String)
    fun setNameTag(player: Player, prefix: String, suffix: String)
    fun clearNameTag(player: Player)
    fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20)
    fun sendActionBar(player: Player, message: String)
    fun sendPacketTitle(player: Player, title: String)
    fun sendPacketSubtitle(player: Player, subtitle: String)
}

interface NmsItemService {
    fun setString(item: ItemStack, key: String, value: String): ItemStack
    fun setInt(item: ItemStack, key: String, value: Int): ItemStack
    fun setLong(item: ItemStack, key: String, value: Long): ItemStack
    fun setDouble(item: ItemStack, key: String, value: Double): ItemStack
    fun setBoolean(item: ItemStack, key: String, value: Boolean): ItemStack
    fun getString(item: ItemStack, key: String): String?
    fun getInt(item: ItemStack, key: String): Int?
    fun getLong(item: ItemStack, key: String): Long?
    fun getDouble(item: ItemStack, key: String): Double?
    fun getBoolean(item: ItemStack, key: String): Boolean?
    fun has(item: ItemStack, key: String): Boolean
    fun remove(item: ItemStack, key: String): ItemStack
    fun getAllKeys(item: ItemStack): Set<String>
}

interface NmsEntityService {
    fun setCustomName(entity: Entity, name: String?)
    fun setInvulnerable(entity: LivingEntity, value: Boolean)
}