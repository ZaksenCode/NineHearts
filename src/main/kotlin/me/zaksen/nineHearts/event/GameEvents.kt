package me.zaksen.nineHearts.event

import io.papermc.paper.advancement.AdvancementDisplay
import me.zaksen.nineHearts.config.ConfigContainer
import me.zaksen.nineHearts.database.DatabaseController
import me.zaksen.nineHearts.util.ChatUtil
import me.zaksen.nineHearts.util.ChatUtil.message
import me.zaksen.nineHearts.util.ChatUtil.title
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import java.util.*

class GameEvents(private val configs: ConfigContainer): Listener {

    private val newPlayers: MutableList<UUID> = mutableListOf()

    @EventHandler
    fun grantAdvancement(event: PlayerAdvancementDoneEvent) {
        if(event.advancement.display?.doesShowToast() == false) {
            return
        }

        when(event.advancement.display?.frame()) {
            AdvancementDisplay.Frame.TASK -> grantAdvancementPoints(event.player, configs.mainConfig().getInt("advancement_reward.task"))
            AdvancementDisplay.Frame.GOAL -> grantAdvancementPoints(event.player, configs.mainConfig().getInt("advancement_reward.goal"))
            AdvancementDisplay.Frame.CHALLENGE -> grantAdvancementPoints(event.player, configs.mainConfig().getInt("advancement_reward.challenge"))
            null -> return
        }
    }

    private fun grantAdvancementPoints(player: Player, points: Int) {
        val data = DatabaseController.getData(player) ?: return
        DatabaseController.updatePoints(player, data.points + points)

        if(configs.mainConfig().getBoolean("advancement_reward.do_toast")) {
            player.message(
                configs.mainConfig().getString("advancement_reward.toast")!!,
                Pair("{points}", points.toString()),
                Pair("{new_points}", (data.points + points).toString())
            )
        }

    }

    @EventHandler
    fun login(event: PlayerLoginEvent) {
        val data = DatabaseController.getData(event.player)

        if(data == null) {
            DatabaseController.createData(event.player.uniqueId)
            newPlayers.add(event.player.uniqueId)
        }
    }

    @EventHandler
    fun join(event: PlayerJoinEvent) {
        val maxHealthAttribute = event.player.getAttribute(Attribute.GENERIC_MAX_HEALTH) ?: return
        maxHealthAttribute.baseValue = 2.0
        event.player.sendHealthUpdate()

        if(newPlayers.contains(event.player.uniqueId)) {
            newPlayers.remove(event.player.uniqueId)

            configs.mainConfig().getStringList("first_join").forEach {
                event.player.message(it)
            }
        } else {
            val data = DatabaseController.getData(event.player)

            event.player.title(
                configs.mainConfig().getString("join.title")!!,
                configs.mainConfig().getString("join.sub_title")!!,
                Pair("{player}", event.player.name),
                Pair("{hearts}", data?.hearts?.toString()!!),
                Pair("{points}", data.points.toString())
            )
        }
    }


    @EventHandler
    fun death(event: PlayerDeathEvent) {
        DatabaseController.withdrawHearts(event.player, 1)

        val broadcastType = configs.mainConfig().getString("death_msg.broadcast.type")!!.uppercase(Locale.getDefault())

        when(broadcastType) {
            "BOTH" -> {
                ChatUtil.broadcast(
                    configs.mainConfig().getString("death_msg.broadcast.chat")!!,
                    Pair("{player}", event.player.name)
                )
                ChatUtil.broadcastTitle(
                    configs.mainConfig().getString("death_msg.broadcast.title")!!,
                    configs.mainConfig().getString("death_msg.broadcast.sub_title")!!,
                    Pair("{player}", event.player.name)
                )
            }
            "TITLE" -> {
                ChatUtil.broadcastTitle(
                    configs.mainConfig().getString("death_msg.broadcast.title")!!,
                    configs.mainConfig().getString("death_msg.broadcast.sub_title")!!,
                    Pair("{player}", event.player.name)
                )
            }
            "CHAT" -> {
                ChatUtil.broadcast(
                    configs.mainConfig().getString("death_msg.broadcast.chat")!!,
                    Pair("{player}", event.player.name)
                )
            }
        }

        val data = DatabaseController.getData(event.player) ?: return
        val messageType = configs.mainConfig().getString("death_msg.type")!!.uppercase(Locale.getDefault())

        when(messageType) {
            "BOTH" -> {
                event.player.message(
                    configs.mainConfig().getString("death_msg.chat")!!,
                    Pair("{hearts}", data.hearts.toString())
                )
                event.player.title(
                    configs.mainConfig().getString("death_msg.title")!!,
                    configs.mainConfig().getString("death_msg.sub_title")!!,
                    Pair("{hearts}", data.hearts.toString())
                )
            }
            "TITLE" -> {
                event.player.title(
                    configs.mainConfig().getString("death_msg.title")!!,
                    configs.mainConfig().getString("death_msg.sub_title")!!,
                    Pair("{hearts}", data.hearts.toString())
                )
            }
            "CHAT" -> {
                event.player.message(
                    configs.mainConfig().getString("death_msg.chat")!!,
                    Pair("{hearts}", data.hearts.toString())
                )
            }
        }
    }

    @EventHandler
    fun totemCheck(event: EntityResurrectEvent) {
        if(event.isCancelled) {
            return
        }

        if(event.entity is Player) {
            DatabaseController.withdrawPoints(event.entity as Player,  configs.mainConfig().getInt("totem_use_cost"))
        }
    }
}