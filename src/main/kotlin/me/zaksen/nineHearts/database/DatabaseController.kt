package me.zaksen.nineHearts.database

import me.zaksen.nineHearts.config.ConfigContainer
import me.zaksen.nineHearts.database.data.PlayerData
import me.zaksen.nineHearts.util.ChatUtil.message
import org.bukkit.GameMode
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Objective
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.UUID
import java.util.logging.Logger

object DatabaseController {

    private val playerCache: MutableMap<UUID, PlayerData> = mutableMapOf()

    private var connection: Connection? = null
    private var mainConfig: FileConfiguration? = null
    private var objective: Objective? = null

    fun setup(logger: Logger, configs: ConfigContainer, objective: Objective) {
        try {
            this.mainConfig = configs.mainConfig()
            this.objective = objective
            connection = DriverManager.getConnection("jdbc:sqlite:${mainConfig?.getString("table_name")}.db")
            buildTables()
        } catch (e: SQLException) {
            logger.warning("Unable to connect database: $e")
        }
    }

    fun stop() {
        connection?.close()
    }

    private fun buildTables() {
        val statement = connection?.prepareStatement(
            """
                CREATE TABLE IF NOT EXISTS ${mainConfig?.getString("table_name")} (
                    uuid TEXT,
                    hearts INT,
                    points INT
                );
                """
        )
        statement?.execute()
        statement?.close()
    }

    fun createData(uuid: UUID){
        val statement = connection?.prepareStatement(
            """
            INSERT INTO ${mainConfig?.getString("table_name")} (uuid, hearts, points)
            VALUES (?, ?, 0); 
            """)
        statement?.setString(1, uuid.toString())
        statement?.setInt(2, mainConfig?.getInt("default_hearts")!!)
        statement?.execute()
        statement?.close()
    }

    fun getData(player: Player): PlayerData? {
        val uuid = player.uniqueId
        val cachedValue = playerCache[uuid]

        if(cachedValue == null) {
            val statement = connection?.prepareStatement("SELECT * FROM ${mainConfig?.getString("table_name")} WHERE uuid = ?")
            statement?.setString(1, uuid.toString())
            val parsed = parsePlayerSet(statement?.executeQuery())
            statement?.close()

            if(parsed != null) {
                objective?.getScore(player)?.score = parsed.points
                playerCache[uuid] = parsed
            }

            return parsed
        } else {
            return cachedValue
        }
    }

    private fun reloadCache(player: Player) {
        val uuid = player.uniqueId
        playerCache.remove(uuid)
        val data = getData(player) ?: return
        playerCache[uuid] = data
        objective?.getScore(player)?.score = data.points
    }

    fun updateData(player: Player, hearts: Int, points: Int) {
        val uuid = player.uniqueId
        val statement = connection?.prepareStatement(
            """
            UPDATE ${mainConfig?.getString("table_name")}
            SET hearts = ?, points = ?
            WHERE uuid = ?; 
            """)
        statement?.setInt(1, hearts)
        statement?.setInt(2, points)
        statement?.setString(3, uuid.toString())
        statement?.execute()
        statement?.close()

        reloadCache(player)
    }

    fun updatePoints(player: Player, points: Int) {
        val uuid = player.uniqueId
        val statement = connection?.prepareStatement(
            """
            UPDATE ${mainConfig?.getString("table_name")}
            SET points = ?
            WHERE uuid = ?; 
            """)
        statement?.setInt(1, points)
        statement?.setString(2, uuid.toString())
        statement?.execute()
        statement?.close()

        reloadCache(player)
    }

    fun updateHearts(player: Player, hearts: Int) {
        val uuid = player.uniqueId
        val statement = connection?.prepareStatement(
            """
            UPDATE ${mainConfig?.getString("table_name")}
            SET hearts = ?
            WHERE uuid = ?; 
            """)
        statement?.setInt(1, hearts)
        statement?.setString(2, uuid.toString())
        statement?.execute()
        statement?.close()

        reloadCache(player)
    }

    fun withdrawPoints(player: Player, points: Int) {
        val data = getData(player) ?: return

        if(data.points - points <= mainConfig?.getInt("heart_lost_debt.count")!!) {
            withdrawHearts(player, 1)
            updatePoints(player, 0)

            if(mainConfig?.getBoolean("heart_lost_debt.do_toast")!!) {
                player.message(mainConfig?.getString("heart_lost_debt.toast")!!, Pair("{hearts}", (data.hearts - 1).toString()))
            }

            return
        }

        updatePoints(player, data.points - points)
    }

    fun withdrawHearts(player: Player, hearts: Int) {
        val data = getData(player) ?: return

        if(data.hearts - hearts <= 0) {
            player.gameMode = GameMode.SPECTATOR
            player.message(mainConfig?.getString("spectator_message")!!)
        }

        updateHearts(player, data.hearts - hearts)
    }

    private fun parsePlayerSet(set: ResultSet?): PlayerData? {
        if(set != null && set.next()) {
            val hearts = set.getInt("hearts")
            val points = set.getInt("points")

            return PlayerData(hearts, points)
        }

        return null
    }
}