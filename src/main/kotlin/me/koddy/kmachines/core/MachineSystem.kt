package me.koddy.kmachines.core

import me.koddy.kmachines.Main
import me.koddy.kmachines.`object`.DropsFuelConfigData
import me.koddy.kmachines.`object`.MachineConfigData
import me.koddy.kmachines.`object`.MachineData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block

object MachineSystem {

    val loadedMachines = mutableMapOf<Block, MachineData>()
    val machineConfig = mutableListOf<MachineConfigData>()
    val dropsLevels = mutableListOf<DropsFuelConfigData>()
    val fuelLevels = mutableListOf<DropsFuelConfigData>()

    fun loadMachines() {
        onDisable()

        for (data in Main.instance.maquinas.getSection("Combustivel").keys) {
            val display = Main.instance.maquinas.getString("Combustivel.$data.Display").replace("&", "§")
            val amount = Main.instance.maquinas.getInt("Combustivel.$data.Amount")
            val price = Main.instance.maquinas.getInt("Combustivel.$data.Price")

            fuelLevels.add(
                DropsFuelConfigData(
                    display,
                    amount,
                    0,
                    price
                )
            )
        }

        for (data in Main.instance.maquinas.getSection("Drops").keys) {
            val display = Main.instance.maquinas.getString("Drops.$data.Display").replace("&", "§")
            val amount = Main.instance.maquinas.getInt("Drops.$data.Amount")
            val delay = Main.instance.maquinas.getInt("Drops.$data.Delay").toLong()
            val price = Main.instance.maquinas.getInt("Drops.$data.Price")

            dropsLevels.add(
                DropsFuelConfigData(
                    display,
                    amount,
                    delay,
                    price
                )
            )
        }

        for (machine in Main.instance.maquinas.getSection("Maquinas").keys) {
            val name = Main.instance.maquinas.getString("Maquinas.$machine.Name")
            val display = Main.instance.maquinas.getString("Maquinas.$machine.Display").replace("&", "§")
            val lore = Main.instance.maquinas.getStringList("Maquinas.$machine.Lore").map { it.replace("&", "§")}.toMutableList()
            val blockType = Material.valueOf(Main.instance.maquinas.getString("Maquinas.$machine.BlockType"))
            val dropType = Material.valueOf(Main.instance.maquinas.getString("Maquinas.$machine.DropType"))
            val dropPrice = Main.instance.maquinas.getInt("Maquinas.$machine.DropPrice")

            machineConfig.add(
                MachineConfigData(
                    name,
                    display,
                    dropPrice,
                    lore,
                    blockType,
                    dropType
                )
            )
        }

        for (id in Main.instance.storage.getSection("Machines").keys) {
            val rawLoc = id.split("_")

            val loc = Location(Bukkit.getWorld(rawLoc[0]), rawLoc[1].toDouble(), rawLoc[2].toDouble(), rawLoc[3].toDouble())

            val owner = Main.instance.storage.getString("Machines.$id.Owner")
            val ownerDisplay = Main.instance.storage.getString("Machines.$id.OwnerDisplay")
            val type = machineConfig.find{ it.name == Main.instance.storage.getString("Machines.$id.Type") }?: continue
            val amount = Main.instance.storage.getInt("Machines.$id.Amount")
            val upgradeDrops = dropsLevels.find { it.display == Main.instance.storage.getString("Machines.$id.Upgrades.Drops") }!!
            val upgradeCombustive =
                fuelLevels.find { it.display == Main.instance.storage.getString("Machines.$id.Upgrades.Combustivel") }!!
            val drops =
                Main.instance.storage.getInt("Machines.$id.GeneratedDrops")

            loadedMachines[loc.block] =
                MachineData(owner, ownerDisplay, type, amount, upgradeDrops, upgradeCombustive, drops, type.dropPrice, loc.block)
        }

        loadedMachines.values.forEach {
            it.updateHolograms()
        }

        startDropsGenerator()
    }

    private fun startDropsGenerator() {
        for (dropLevel in dropsLevels) {
            Main.instance.syncTimer(20 * dropLevel.delay, 20 * dropLevel.delay) {

                for (machine in loadedMachines.values.filter {
                    it.upgradeDrops == dropLevel && it.combustivel > 0
                }) {
                    machine.drops += dropLevel.amount
                    machine.combustivel--
                    machine.nextGenerate = (System.currentTimeMillis() + 1000L * dropLevel.delay)

                    machine.saveToDisk()
                    machine.updateHolograms()
                }
            }
        }

    }

    fun onDisable() {
        loadedMachines.values.forEach {
            it.holograms.forEach { stand ->
                stand.chunk.isForceLoaded = true
                if (!stand.chunk.isLoaded) {
                    stand.chunk.load(true)
                }

                stand.remove()
            }
            it.holograms.clear()
        }

        loadedMachines.clear()
        machineConfig.clear()
    }
}