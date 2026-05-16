package io.zlero.cRFramework.nms

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

/**
 * Paper PersistentDataContainer 기반 NBT 구현 (1.14+, Reflection 불필요)
 */
internal class PersistentDataItemService(
    private val namespace: String = "crframework"
) : NmsItemService {

    private fun key(k: String) = NamespacedKey(namespace, k)

    override fun setString(item: ItemStack, key: String, value: String)   = item.edit { it.persistentDataContainer.set(key(key), PersistentDataType.STRING, value) }
    override fun setInt(item: ItemStack, key: String, value: Int)         = item.edit { it.persistentDataContainer.set(key(key), PersistentDataType.INTEGER, value) }
    override fun setLong(item: ItemStack, key: String, value: Long)       = item.edit { it.persistentDataContainer.set(key(key), PersistentDataType.LONG, value) }
    override fun setDouble(item: ItemStack, key: String, value: Double)   = item.edit { it.persistentDataContainer.set(key(key), PersistentDataType.DOUBLE, value) }
    override fun setBoolean(item: ItemStack, key: String, value: Boolean) = item.edit { it.persistentDataContainer.set(key(key), PersistentDataType.BOOLEAN, value) }

    override fun getString(item: ItemStack, key: String)  = item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.STRING)
    override fun getInt(item: ItemStack, key: String)     = item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.INTEGER)
    override fun getLong(item: ItemStack, key: String)    = item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.LONG)
    override fun getDouble(item: ItemStack, key: String)  = item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.DOUBLE)
    override fun getBoolean(item: ItemStack, key: String) = item.itemMeta?.persistentDataContainer?.get(key(key), PersistentDataType.BOOLEAN)

    override fun has(item: ItemStack, key: String): Boolean =
        item.itemMeta?.persistentDataContainer?.has(key(key)) ?: false

    override fun remove(item: ItemStack, key: String) = item.edit { it.persistentDataContainer.remove(key(key)) }

    override fun getAllKeys(item: ItemStack): Set<String> =
        item.itemMeta?.persistentDataContainer?.keys
            ?.filter { it.namespace == namespace }
            ?.map { it.key }
            ?.toSet() ?: emptySet()

    private fun ItemStack.edit(block: (ItemMeta) -> Unit): ItemStack {
        val copy = this.clone()
        val meta = copy.itemMeta ?: return copy
        block(meta)
        copy.itemMeta = meta
        return copy
    }
}