package me.zaksen.nineHearts.command

import me.zaksen.nineHearts.config.ConfigContainer
import me.zaksen.nineHearts.menu.HeartsBuyMenu
import me.zaksen.nineHearts.menu.MenuController
import me.zaksen.nineHearts.util.ChatUtil.message
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BuyLifeCommand(private val configs: ConfigContainer): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if(!sender.hasPermission("hearts.command.buy")) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_permission")!!)
            return true
        }

        if(sender is Player) {
            MenuController.openMenu(sender, HeartsBuyMenu(sender, configs))
        }

        return true
    }

}