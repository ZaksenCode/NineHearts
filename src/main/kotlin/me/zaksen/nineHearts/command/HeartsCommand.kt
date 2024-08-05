package me.zaksen.nineHearts.command

import me.zaksen.nineHearts.config.ConfigContainer
import me.zaksen.nineHearts.database.DatabaseController
import me.zaksen.nineHearts.menu.HeartsBuyMenu
import me.zaksen.nineHearts.menu.MenuController
import me.zaksen.nineHearts.util.ChatUtil.message
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class HeartsCommand(private val configs: ConfigContainer): TabExecutor {

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String> {
        when(args[0]) {
            "set",
            "add",
            "remove" -> {
                Bukkit.getOnlinePlayers()
            }
        }

        if(args.size > 1) {
            when (args[1]) {
                "set",
                "add",
                "remove" -> {
                    mutableListOf(1)
                }
            }
        }

        return mutableListOf("set", "add", "remove", "reload", "status")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.isEmpty()) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_args")!!)
        }

        when(args[0]) {
            "set" -> {
                setHearts(sender, Bukkit.getPlayer(args[1]), args[2].toInt())
            }
            "add" -> {
                addHearts(sender, Bukkit.getPlayer(args[1]), args[2].toInt())
            }
            "remove" -> {
                removeHearts(sender, Bukkit.getPlayer(args[1]), args[2].toInt())
            }
            "reload" -> {
                reloadHearts(sender)
            }
            "status" -> {
                statusHearts(sender)
            }
        }

        return true
    }

    private fun setHearts(sender: CommandSender, player: Player?, value: Int) {
        if(!sender.hasPermission("hearts.command.set")) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_permission")!!)
            return
        }

        if(player == null) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_player")!!)
            return
        }

        DatabaseController.updateHearts(player, value)

        sender.message(
            configs.mainConfig().getString("hearts_command.success")!!,
            Pair("{player}", player.name),
            Pair("{hearts}", value.toString())
        )
    }

    private fun addHearts(sender: CommandSender, player: Player?, value: Int) {
        if(!sender.hasPermission("hearts.command.add")) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_permission")!!)
            return
        }

        if(player == null) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_player")!!)
            return
        }

        val data = DatabaseController.getData(player) ?: return
        DatabaseController.updateHearts(player, data.hearts + value)

        sender.message(
            configs.mainConfig().getString("hearts_command.success")!!,
            Pair("{player}", player.name),
            Pair("{hearts}", data.hearts.toString())
        )
    }

    private fun removeHearts(sender: CommandSender, player: Player?, value: Int) {
        if(!sender.hasPermission("hearts.command.remove")) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_permission")!!)
            return
        }

        if(player == null) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_player")!!)
            return
        }

        val data = DatabaseController.getData(player) ?: return
        DatabaseController.updateHearts(player, data.hearts - value)

        sender.message(
            configs.mainConfig().getString("hearts_command.success")!!,
            Pair("{player}", player.name),
            Pair("{hearts}", data.hearts.toString())
        )
    }

    private fun reloadHearts(sender: CommandSender) {
        if(!sender.hasPermission("hearts.command.reload")) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_permission")!!)
            return
        }

        configs.reloadConfigs()
        sender.message(configs.mainConfig().getString("hearts_command.success_reload")!!)
    }

    private fun statusHearts(sender: CommandSender) {
        if(!sender.hasPermission("hearts.command.status")) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_no_permission")!!)
            return
        }

        if(sender !is Player) {
            sender.message(configs.mainConfig().getString("hearts_command.fail_sender_not_player")!!)
        }

        val data = DatabaseController.getData(sender as Player) ?: return

        sender.message(
            configs.mainConfig().getString("hearts_command.success_status")!!,
            Pair("{hearts}", data.hearts.toString()),
            Pair("{points}", data.points.toString())
        )
    }
}