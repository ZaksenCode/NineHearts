package me.zaksen.nineHearts

import me.zaksen.nineHearts.command.BuyLifeCommand
import me.zaksen.nineHearts.command.HeartsCommand
import me.zaksen.nineHearts.command.HeartsPointsCommand
import me.zaksen.nineHearts.config.ConfigContainer
import me.zaksen.nineHearts.database.DatabaseController
import me.zaksen.nineHearts.event.GameEvents
import me.zaksen.nineHearts.event.MenuEvents
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

class NineHearts : JavaPlugin(), ConfigContainer {

    private var objective: Objective? = null

    override fun onEnable() {
        saveDefaultConfig()
        loadScoreboard()
        DatabaseController.setup(logger, this, objective!!)

        server.pluginManager.registerEvents(GameEvents(this), this)
        server.pluginManager.registerEvents(MenuEvents(), this)

        getCommand("hearts")?.setExecutor(HeartsCommand(this))
        getCommand("hearts")?.tabCompleter = HeartsCommand(this)

        getCommand("hearts_points")?.setExecutor(HeartsPointsCommand(this))
        getCommand("hearts_points")?.tabCompleter = HeartsPointsCommand(this)

        getCommand("buylife")?.setExecutor(BuyLifeCommand(this))
    }

    override fun onDisable() {
        DatabaseController.stop()
    }

    override fun reloadConfigs() {
        reloadConfig()
    }

    override fun mainConfig(): FileConfiguration {
        return config
    }

    private fun loadScoreboard() {
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

        if(scoreboard.getObjective("name_points") == null) {
            objective = scoreboard.registerNewObjective("name_points", Criteria.DUMMY, Component.text("Name points"))
            objective!!.displaySlot = DisplaySlot.PLAYER_LIST
        } else {
            objective = scoreboard.getObjective("name_points")
        }
    }
}
