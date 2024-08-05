package me.zaksen.nineHearts.config

import org.bukkit.configuration.file.FileConfiguration

interface ConfigContainer {
    fun reloadConfigs()

    fun mainConfig(): FileConfiguration
}