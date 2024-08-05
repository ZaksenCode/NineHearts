package me.zaksen.nineHearts.menu

import me.zaksen.nineHearts.config.ConfigContainer
import me.zaksen.nineHearts.database.DatabaseController
import me.zaksen.nineHearts.menu.api.Menu
import me.zaksen.nineHearts.menu.api.MenuType
import me.zaksen.nineHearts.stack.loreMap
import me.zaksen.nineHearts.stack.name
import me.zaksen.nineHearts.util.ChatUtil
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class HeartsBuyMenu(viewer: Player, configs: ConfigContainer): Menu(MenuType.BASE_27, ChatUtil.format(configs.mainConfig().getString("buy_menu.title")!!)) {

    init {
        val heartLimit: Int = configs.mainConfig().getInt("hearts_buy_limit")
        val price: Int = configs.mainConfig().getInt("heart_price")
        val data = DatabaseController.getData(viewer)

        if (data != null) {
            fill(
                ItemStack(
                    Material.valueOf(configs.mainConfig().getString("buy_menu.background.material")!!.uppercase(Locale.getDefault()))
                ).name(ChatUtil.format(configs.mainConfig().getString("buy_menu.background.name")!!))
            )
            setItem(
                configs.mainConfig().getInt("buy_menu.buy_item.slot"),
                ItemStack(
                    Material.valueOf(configs.mainConfig().getString("buy_menu.buy_item.material")!!.uppercase(Locale.getDefault()))
                ).name(ChatUtil.format(configs.mainConfig().getString("buy_menu.buy_item.name")!!))
                    .loreMap(makeLore(
                        configs.mainConfig().getStringList("buy_menu.buy_item.lore"),
                        Pair("{price}", price.toString()),
                        Pair("{hearts}", data.hearts.toString()),
                        Pair("{points}", data.points.toString())
                    ))
            ) {
                if(data.hearts >= heartLimit) {
                    viewer.sendMessage(ChatUtil.format(configs.mainConfig().getString("buy.fail_limit")!!))
                    viewer.closeInventory()
                    return@setItem
                }

                if(data.points >= price) {
                    DatabaseController.updateData(
                        viewer,
                        data.hearts + 1,
                        data.points - price
                    )
                    viewer.sendMessage(ChatUtil.format(configs.mainConfig().getString("buy.success")!!))
                    viewer.closeInventory()
                } else {
                    viewer.sendMessage(ChatUtil.format(configs.mainConfig().getString("buy.fail")!!, Pair("{price}", price.toString())))
                    viewer.closeInventory()
                }
            }
        }
        updateInventory()
    }

    private fun makeLore(list: List<String>, vararg args: Pair<String, String>): List<Component> {
        val result: MutableList<Component> = mutableListOf()

        for(loreLine: String in list) {
            result.add(ChatUtil.format(loreLine, *args))
        }

        return result
    }
}