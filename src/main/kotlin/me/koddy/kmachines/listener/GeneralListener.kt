package me.koddy.kmachines.listener

import com.mikael.mkutils.spigot.api.*
import com.mikael.mkutils.spigot.api.lib.MineItem
import com.mikael.mkutils.spigot.api.lib.MineListener
import me.koddy.kmachines.Main
import me.koddy.kmachines.api.isMachine
import me.koddy.kmachines.api.machine
import me.koddy.kmachines.api.nearbyBlocks
import me.koddy.kmachines.core.CombustivelSystem
import me.koddy.kmachines.core.MachineSystem
import me.koddy.kmachines.menus.MachineMenu
import me.koddy.kmachines.`object`.MachineData
import net.eduard.api.lib.kotlin.mineSetLore
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class GeneralListener : MineListener() {
    val cooldown = mutableListOf<Player>()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onClickMachine(e: PlayerInteractEvent) {
        val player = e.player

        player.runBlock {
            if (!e.action.isRightClick) return@runBlock
            val block = e.clickedBlock ?: return@runBlock
            val item = player.inventory.itemInMainHand

            if (item.type == Material.COAL) {

                val coalData = item.itemMeta.persistentDataContainer.get(
                    NamespacedKey(Main.instance.pluginConnected, "combustivel"),
                    PersistentDataType.STRING
                )

                if (coalData != null) {
                    player.runBlock {

                        if (!cooldown.contains(player)) {

                            val coal = CombustivelSystem.combustiveis.find {
                                it.name == coalData.split(";")[0]
                            }!!

                            block.machine?.let { machine ->
                                val machineFuelFree = (machine.upgradeCombustive.amount * machine.amount) - machine.combustivel
                                val fuelRestante = coal.litros - machineFuelFree
                                val canAdd = coal.litros - fuelRestante

                                if (machineFuelFree == 0) {
                                    player.sendMessage("§cEsta máquina já está cheia.")

                                    return@let
                                }

                                if(canAdd > coal.litros) {
                                    machine.combustivel += coal.litros
                                    machine.updateHolograms()

                                    if (item.amount == 1) {
                                        player.inventory.remove(item)
                                    } else {
                                        item.amount--
                                    }

                                    cooldown.add(player)
                                    Main.instance.syncDelay(5) {
                                        cooldown.remove(player)
                                    }

                                    player.soundYes()
                                    player.sendMessage("§aVocê abasteu §e${canAdd} litros §a de §e${coal.name}§a.")

                                    return@let
                                }

                                machine.combustivel += canAdd
                                machine.updateHolograms()

                                val meta = item.itemMeta

                                meta.persistentDataContainer.set(
                                    NamespacedKey(Main.instance, "combustivel"),
                                    PersistentDataType.STRING,
                                    "${coal.name};${coal.litros - canAdd};${coal.infinity};"
                                )
                                item.itemMeta = meta

                                if(fuelRestante == 0) {
                                    if (item.amount == 1) {
                                        player.inventory.remove(item)
                                    } else {
                                        item.amount--
                                    }
                                } else {
                                    val lore = coal.oriLore.toMutableList().map { it.replace("{litros}", coal.litros.toString()).replace("&", "§") }.toMutableList()
                                    item.mineSetLore(*lore.toTypedArray())
                                }


                                cooldown.add(player)
                                Main.instance.syncDelay(5) {
                                    cooldown.remove(player)
                                }

                                player.soundYes()
                                player.sendMessage("§aVocê abasteu §e${canAdd} litros §a de §e${coal.name}§a.")
                            }
                        }
                    }

                    return@runBlock
                }
            }

            block.machine?.let { machine ->
                MachineMenu.getMenu(machine).open(player)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlaceMachine(e: BlockPlaceEvent) {
        val player = e.player

        player.runBlock {
            val item = e.itemInHand

            if (!item.isMachine) return@runBlock

            val nearby = e.block.location.nearbyBlocks

            val machineData = item.itemMeta.persistentDataContainer.get(
                NamespacedKey(Main.instance.pluginConnected, "machine"),
                PersistentDataType.STRING
            )!!.split(";")

            if (player.isSneaking) {
                val machine = MachineData(
                    player.name.lowercase(),
                    player.name,
                    MachineSystem.machineConfig.find { it.name == machineData[0] }!!,
                    machineData[1].toInt() * item.amount,
                    MachineSystem.dropsLevels.find { it.display == machineData[2] }!!,
                    MachineSystem.fuelLevels.find { it.display == machineData[3] }!!,
                    machineData[4].toInt(),
                    MachineSystem.machineConfig.find { it.name == machineData[0] }!!.dropPrice,
                    e.block
                )

                for (nearbyBlock in nearby) {
                    val possibleMachine = nearbyBlock.machine ?: continue

                    if (machine.type != possibleMachine.type) continue;

                    possibleMachine.amount += machine.amount
                    possibleMachine.saveToDisk()
                    possibleMachine.updateHolograms()

                    Main.instance.syncDelay(1) {
                        player.runBlock {
                            e.block.type = Material.AIR
                        }
                    }
                    if (item.amount > 1) {
                        player.inventory.remove(item)
                    }
                    player.sendMessage("§aVocê stackou x${machine.amount} de maquina(s) de ${machine.type.display}§a. Nova quantidade: x${possibleMachine.amount}")
                    return@runBlock
                }

                if (item.amount > 1) {
                    player.inventory.remove(item)
                }

                machine.saveToDisk() // salvar no storage.yml
                MachineSystem.loadedMachines[e.block] = machine
                machine.updateHolograms()

                player.sendMessage("§aVocê adicionou x${machine.amount} de maquina(s) de ${machine.type.display}§a.")
                return@runBlock
            }

            val machine = MachineData(
                player.name.lowercase(),
                player.name,
                MachineSystem.machineConfig.find { it.name == machineData[0] }!!,
                machineData[1].toInt(),
                MachineSystem.dropsLevels.find { it.display == machineData[2] }!!,
                MachineSystem.fuelLevels.find { it.display == machineData[3] }!!,
                machineData[4].toInt(),
                MachineSystem.machineConfig.find { it.name == machineData[0] }!!.dropPrice,
                e.block
            )

            for (nearbyBlock in nearby) {
                val possibleMachine = nearbyBlock.machine ?: continue

                if (machine.type != possibleMachine.type) continue;
                if (machine.upgradeCombustive != possibleMachine.upgradeCombustive) continue;
                if (machine.upgradeDrops != possibleMachine.upgradeDrops) continue;

                possibleMachine.amount += machine.amount
                possibleMachine.saveToDisk()
                possibleMachine.updateHolograms()

                Main.instance.syncDelay(1) {
                    player.runBlock {
                        e.block.type = Material.AIR
                    }
                }
                player.sendMessage("§aVocê stackou x${machine.amount} de maquina(s) de ${machine.type.display}§a. Nova quantidade: x${possibleMachine.amount}")
                return@runBlock
            }

            machine.saveToDisk() // salvar no storage.yml
            MachineSystem.loadedMachines[e.block] = machine
            machine.updateHolograms()

            player.sendMessage("§aVocê adicionou x${machine.amount} de maquina(s) de ${machine.type.display}§a.")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBreakMachine(e: BlockBreakEvent) {
        val player = e.player

        player.runBlock {
            val block = e.block
            val machine = block.machine ?: return@runBlock;

            val item = MineItem(machine.type.BlockType)

            item.name("§aMaquina de ${machine.type.display}")
            item.lore("§fTipo: ${machine.type.display}", "§fQuantidade: §e${machine.amount}")

            val meta = item.itemMeta
            meta.persistentDataContainer.set(
                NamespacedKey(Main.instance, "machine"),
                PersistentDataType.STRING,
                "${machine.type.name};${machine.amount};${machine.upgradeDrops.display};${machine.upgradeCombustive.display};0"
            )

            item.itemMeta = meta

            machine.delete()

            e.isDropItems = false
            player.giveItem(item)
        }
    }
}