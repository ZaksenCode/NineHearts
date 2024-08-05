package me.zaksen.nineHearts.event

import me.zaksen.nineHearts.menu.MenuController
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class MenuEvents: Listener {

    @EventHandler
    fun processContainerOpenEvent(event: InventoryOpenEvent) {
        MenuController.processMenuOpen(event)
    }

    @EventHandler
    fun clearContainerOnClose(event: InventoryCloseEvent) {
        if(MenuController.getMenu(event.player) != null) {
            MenuController.processMenuClose(event)
            MenuController.forgetMenu(event.player)
        }
    }

    @EventHandler
    fun processContainerClicks(event: InventoryClickEvent) {
        MenuController.processMenuClick(event)
    }
}