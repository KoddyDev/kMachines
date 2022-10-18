package me.koddy.kmachines.menus

import com.mikael.mkutils.spigot.api.lib.MineItem
import com.mikael.mkutils.spigot.api.lib.menu.MineMenu
import com.mikael.mkutils.spigot.api.runBlock
import com.mikael.mkutils.spigot.api.soundNo
import com.mikael.mkutils.spigot.api.soundYes
import me.koddy.kmachines.Main
import me.koddy.kmachines.core.MachineSystem
import net.eduard.api.lib.kotlin.format
import org.bukkit.Material
import org.bukkit.entity.Player

class DropsMenu(var player: Player) : MineMenu("Menu de Drops", 3) {
    companion object {
        lateinit var instance: MineMenu
        val menus = mutableMapOf<Player, DropsMenu>()

        fun getMenu(player: Player): DropsMenu {
            if (menus.containsKey(player)) return menus[player]!!

            val newMenu = DropsMenu(player)
            newMenu.registerMenu(Main.instance)
            menus[player] = newMenu

            return newMenu
        }
    }

    init {
        instance = this@DropsMenu
        isAutoUpdate = true
    }

    override fun update(player: Player) {
        removeAllButtons(player)

        val drops = MachineSystem.loadedMachines.values.filter {
            it.owner == player.name.lowercase()
        }

        button("getDrops") {
            val dropsGive = drops.sumOf { it.drops }

            setPosition(5, 2)

            icon = MineItem(Material.HOPPER)
                .name("§bRecolher Drops")
                .lore("§fClique para recolher §c${dropsGive}x§f drops.")

            click = click@{
                player.runBlock {
                    val give = drops.sumOf { it.drops * it.dropPrice }

                    if(give == 0) {
                        player.soundNo()
                        player.sendMessage("§cVocê não possue drops para vender.")

                        return@runBlock
                    }

                    drops.forEach { it.drops = 0 }
                    Main.instance.econ.depositPlayer(player, give.toDouble())

                    player.soundYes()
                    player.sendMessage("§aVocê vendeu §e${dropsGive}x§a drops e ganhou §e${give.format(true)}")
                }
            }
        }
    }
}