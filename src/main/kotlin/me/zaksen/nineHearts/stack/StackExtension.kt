package me.zaksen.nineHearts.stack

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.name(name: Component): ItemStack {
    val meta: ItemMeta = this.itemMeta
    meta.displayName(name)
    itemMeta = meta
    return this
}

fun ItemStack.loreMap(lore: List<Component>): ItemStack {
    val meta: ItemMeta = this.itemMeta
    meta.lore(lore)
    itemMeta = meta
    return this
}