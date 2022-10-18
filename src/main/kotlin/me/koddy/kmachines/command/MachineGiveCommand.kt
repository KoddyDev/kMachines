package me.koddy.kmachines.command

import com.mikael.mkutils.spigot.api.giveItem
import com.mikael.mkutils.spigot.api.lib.MineItem
import com.mikael.mkutils.spigot.api.runCommand
import com.mikael.mkutils.spigot.api.soundNo
import com.mikael.mkutils.spigot.api.soundYes
import me.koddy.kmachines.Main
import me.koddy.kmachines.core.CombustivelSystem
import me.koddy.kmachines.core.MachineSystem
import net.eduard.api.lib.manager.CommandManager
import net.eduard.api.lib.modules.Mine
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class MachineGiveCommand : CommandManager("give") {
    init {
        usage = "/maquinas give <maquinas/coal> <player> <tipo> <quantidade>"
        permission = "kmachines.admin"
        permissionMessage = "§a${Main.instance.systemName} developed by KoddyDev"
    }

    override fun playerCommand(player: Player, args: Array<String>) {
        player.runCommand {
            if(args.size < 4) {
                player.soundNo()
                sendUsage(player)
                return@runCommand
            }

            val typeGive = args[0]

            if(typeGive.lowercase() != "maquinas" && typeGive.lowercase() != "coal") {
                player.soundNo()
                sendUsage(player)
            }

            val target = Mine.getPlayers().firstOrNull {
                it.name.lowercase() == args[1].lowercase()
            }

            if (target == null) {
                player.soundNo()
                player.sendMessage("§cEste jogador não está online!")
                return@runCommand
            }

            val amount = args[3].toIntOrNull()

            if (amount == null) {
                player.soundNo()
                sendUsage(player)
                return@runCommand
            }

            if(typeGive == "maquinas") {
                val type = MachineSystem.machineConfig.firstOrNull { it.name.lowercase() == args[2].lowercase() }
                if (type == null) {
                    player.soundNo()
                    player.sendMessage("§cTipos invalidos! Tente um destes ->")
                    MachineSystem.machineConfig.forEach {
                        player.sendMessage("§e${it.name}")
                    }
                    return@runCommand
                }

                val item = MineItem(type.BlockType)

                item.name("§aMaquina de ${type.display}")
                item.lore("§fTipo: ${type.display}", "§fQuantidade: §e$amount")

                val meta = item.itemMeta
                meta.persistentDataContainer.set(
                    NamespacedKey(Main.instance, "machine"),
                    PersistentDataType.STRING,
                    "${type.name};${amount};${MachineSystem.dropsLevels.first().display};${MachineSystem.fuelLevels.first().display};0"
                )

                item.itemMeta = meta

                target.giveItem(item)

                player.sendMessage("§aEnviados x${amount} maquina(s) de ${type.display}§a para §f${target.name}§a.")
            } else {
                val type = CombustivelSystem.combustiveis.firstOrNull { ChatColor.stripColor(it.name.uppercase()) == args[2].uppercase() }

                if (type == null) {
                    player.soundNo()
                    player.sendMessage("§cTipos invalidos! Tente um destes ->")
                    CombustivelSystem.combustiveis.forEach {
                        player.sendMessage("§e${it.name}")
                    }
                    return@runCommand
                }

                val item = type.item.clone()
                item.amount(amount)

                target.giveItem(item)
                player.soundYes()
                player.sendMessage("§aEnviados x${amount} combustivel(is) de ${type.name}§a para §f${target.name}§a.")
            }
        }
    }
}