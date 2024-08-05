package me.zaksen.nineHearts.command

import me.zaksen.nineHearts.config.ConfigContainer
import me.zaksen.nineHearts.database.DatabaseController
import me.zaksen.nineHearts.util.ChatUtil.message
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class HeartsPointsCommand(private val configs: ConfigContainer): TabExecutor {

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

        return mutableListOf("set", "add", "remove")
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

        DatabaseController.updatePoints(player, value)

        sender.message(
            configs.mainConfig().getString("hearts_points_cmd.success")!!,
            Pair("{player}", player.name),
            Pair("{points}", value.toString())
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
        DatabaseController.updatePoints(player, data.points + value)

        sender.message(
            configs.mainConfig().getString("hearts_points_cmd.success")!!,
            Pair("{player}", player.name),
            Pair("{points}", data.hearts.toString())
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
        DatabaseController.updatePoints(player, data.points - value)

        sender.message(
            configs.mainConfig().getString("hearts_points_cmd.success")!!,
            Pair("{player}", player.name),
            Pair("{points}", data.hearts.toString())
        )
    }
}