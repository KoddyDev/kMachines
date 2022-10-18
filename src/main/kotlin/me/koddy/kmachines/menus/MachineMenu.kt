package me.koddy.kmachines.menus

import com.mikael.mkutils.api.formatDuration
import com.mikael.mkutils.api.formatEN
import com.mikael.mkutils.spigot.api.lib.MineItem
import com.mikael.mkutils.spigot.api.lib.menu.MineMenu
import com.mikael.mkutils.spigot.api.runBlock
import com.mikael.mkutils.spigot.api.soundNo
import com.mikael.mkutils.spigot.api.soundYes
import me.koddy.kmachines.Main
import me.koddy.kmachines.core.MachineSystem
import me.koddy.kmachines.`object`.MachineData
import net.eduard.api.lib.kotlin.format
import org.bukkit.Material
import org.bukkit.entity.Player

class MachineMenu(var machine: MachineData) : MineMenu("Menu da Máquina", 4) {
    companion object {
        lateinit var instance: MineMenu
        val menus = mutableMapOf<MachineData, MachineMenu>()

        fun getMenu(machineData: MachineData): MachineMenu {
            if (menus.containsKey(machineData)) return menus[machineData]!!

            val newMenu = MachineMenu(machineData)
            newMenu.registerMenu(Main.instance)
            menus[machineData] = newMenu

            return newMenu
        }
    }

    init {
        instance = this@MachineMenu
        isAutoUpdate = true
    }

    override fun update(player: Player) {
        removeAllButtons(player)

        button("papel") {
            setPosition(5, 1)

            val generating = (machine.nextGenerate - System.currentTimeMillis()).formatDuration()
            val generatingText =
                if (machine.nextGenerate == 0L) "§eCarregando..." else
                    if (generating == "-1") "§eGerando drops..." else
                        "§eGerando drops em §f${generating}."

            icon = MineItem(Material.PAPER)
                .name("§bInformações")
                .lore(
                    "§fMaquina de ${player.name}",
                    "§fTipo: ${machine.type.display}",
                    "§fQuantidade: §a${machine.amount}",
                    generatingText
                )

            click = infoClick@{
                player.sendMessage("Next: ${machine.nextGenerate} Atual: ${System.currentTimeMillis()} Diferença: ${machine.nextGenerate - System.currentTimeMillis()}")
            }
        }

        button("upgradeDrops") {
            setPosition(3, 3)

            val nextLevel = if(machine.upgradeDrops == MachineSystem.dropsLevels.last()) MachineSystem.dropsLevels.last() else MachineSystem.dropsLevels[
                    MachineSystem.dropsLevels.indexOf(machine.upgradeDrops)+1
            ]
            icon = MineItem(machine.type.DropType)
                .name("§bDrops")
                .lore(
                    "§fNível: §e${machine.upgradeDrops.display}§f/§b${MachineSystem.dropsLevels.last().display}",
                    "§fEsta máquina gera §e${machine.upgradeDrops.amount}x §fdrops a cada §e${(machine.upgradeDrops.delay)}s§f.",
                    "§f",
                    if(machine.upgradeDrops == nextLevel) "§fVocê não pode §caprimorar§f a quantidade de drops." else "§fCusto para aprimorar: §e${machine.upgradeDrops.price.format(true)}"
                )


            click = click@{
                player.runBlock {
                    if (machine.upgradeDrops.display == MachineSystem.dropsLevels.last().display) {
                        player.soundNo();
                        player.sendMessage("§cEsta máquina já está com no aprimoramento máximo de drops!")

                        return@runBlock
                    }
                    val econ = Main.instance.econ

                    if(!econ.has(player, machine.upgradeDrops.price.toDouble())) {
                        player.soundNo();
                        player.sendMessage("§cVocê não possui coins suficientes para aprimorar a geração de drops!")
                        return@runBlock
                    }

                    econ.withdrawPlayer(player, machine.upgradeDrops.price.toDouble())

                    val newLevel = MachineSystem.dropsLevels[
                            MachineSystem.dropsLevels.indexOf(machine.upgradeDrops) + 1
                    ]
                    machine.upgradeDrops = newLevel;

                    machine.saveToDisk()
                    player.soundYes()
                    player.sendMessage("§aVocê aprimorou a quantidade de drops para ${machine.upgradeDrops.amount}x a cada ${machine.upgradeDrops.delay} segundos.")
                    open(player)
                }
            }
        }

        button("getDrops") {
            setPosition(5, 3)

            icon = MineItem(Material.HOPPER)
                .name("§bRecolher Drops")
                .lore("§fClique para recolher §c${machine.drops}x§f drops.")

            click = click@{
                player.runBlock {
                    val give = machine.drops * machine.dropPrice

                    if (give == 0) {
                        player.soundNo()
                        player.sendMessage("§cVocê não possue drops para vender.")

                        return@runBlock
                    }

                    machine.drops = 0
                    Main.instance.econ.depositPlayer(player, give.toDouble())

                    player.soundYes()
                    player.sendMessage(
                        "§aVocê vendeu §e${(give / machine.dropPrice)}x§a drops e ganhou §e${
                            give.format(
                                true
                            )
                        }"
                    )
                    MachineMenu.getMenu(machine).open(player)
                }
            }
        }

        button("upgradeCombustivel") {
            setPosition(7, 3)

            val nextLevel = if(machine.upgradeCombustive == MachineSystem.fuelLevels.last()) MachineSystem.dropsLevels.last() else MachineSystem.fuelLevels[
                    MachineSystem.fuelLevels.indexOf(machine.upgradeCombustive)+1
            ]

            icon = MineItem(Material.COAL)
                .name("§bCombustivel")
                .lore(
                    "§fNível: ${machine.upgradeCombustive.display}/${MachineSystem.fuelLevels.last().display}",
                    "§fEsta máquina possue",
                    "§fcapacidade de armazenar ${(machine.upgradeCombustive.amount * machine.amount).formatEN()} litros de combustível.",
                    "§f",
                    if(machine.upgradeCombustive == nextLevel) "§fVocê não pode §caprimorar§f a quantidade de drops." else "§fCusto para aprimorar: §e${nextLevel.price.format(true)}"
                )

            click = click@{
                player.runBlock {
                    if (machine.upgradeCombustive.display == MachineSystem.fuelLevels.last().display) {
                        player.soundNo();
                        player.sendMessage("§cEsta máquina já está com no aprimoramento máximo de combustivel")

                        return@runBlock
                    }
                    val econ = Main.instance.econ

                    if(!econ.has(player, machine.upgradeCombustive.price.toDouble())) {
                        player.soundNo();
                        player.sendMessage("§cVocê não possui coins suficientes para aprimorar a geração de drops!")
                        return@runBlock
                    }

                    econ.withdrawPlayer(player, machine.upgradeCombustive.price.toDouble())

                    val newLevel = MachineSystem.dropsLevels[
                            MachineSystem.dropsLevels.indexOf(machine.upgradeCombustive) + 1
                    ]
                    machine.upgradeCombustive = newLevel;
                    machine.saveToDisk()
                    player.soundYes()
                    player.sendMessage("§aVocê aprimorou a quantidade de armazenamento de combustivel para §e${machine.upgradeCombustive.amount} litros§a.")
                    open(player)
                }
            }
        }
    }

}