package me.koddy.kmachines.`object`

import com.mikael.mkutils.api.formatEN
import com.mikael.mkutils.spigot.api.newHologram
import me.koddy.kmachines.Main
import me.koddy.kmachines.core.MachineSystem
import net.eduard.api.lib.modules.Mine
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player

class MachineData(
    var owner: String,
    var ownerDisplay: String,
    var type: MachineConfigData,
    var amount: Int,
    var upgradeDrops: DropsFuelConfigData,
    var upgradeCombustive: DropsFuelConfigData,
    var drops: Int,
    var dropPrice: Int,
    var block: Block,
    var holograms: MutableList<ArmorStand> = mutableListOf(),
    var combustivel: Int = 0,
    var nextGenerate: Long = 0L,

    val ownerPlayer: Player? = Mine.getPlayers().firstOrNull {
        it.name.lowercase() == owner
    }
) {
    fun saveToDisk() {
        Main.instance.storage["Machines.${block.world.name}_${block.x}_${block.y}_${block.z}.Owner"] = owner
        Main.instance.storage["Machines.${block.world.name}_${block.x}_${block.y}_${block.z}.OwnerDisplay"] =
            ownerDisplay
        Main.instance.storage["Machines.${block.world.name}_${block.x}_${block.y}_${block.z}.Type"] = type.name
        Main.instance.storage["Machines.${block.world.name}_${block.x}_${block.y}_${block.z}.Amount"] = amount
        Main.instance.storage["Machines.${block.world.name}_${block.x}_${block.y}_${block.z}.DropPrice"] = dropPrice
        Main.instance.storage["Machines.${block.world.name}_${block.x}_${block.y}_${block.z}.Upgrades.Drops"] =
            upgradeDrops.display
        Main.instance.storage["Machines.${block.world.name}_${block.x}_${block.y}_${block.z}.Upgrades.Combustivel"] =
            upgradeCombustive.display
        Main.instance.storage["Machines.${block.world.name}_${block.x}_${block.y}_${block.z}.GeneratedDrops"] =
            drops


        Main.instance.storage.saveConfig()
    }

    fun updateHolograms() {

        holograms.forEach { stand ->
            stand.chunk.isForceLoaded = true
            if (!stand.chunk.isLoaded) {
                stand.chunk.load(true)
            }

            stand.remove()
        }
        holograms.clear()

        val holoLoc = block.location.toCenterLocation().add(0.0, 1.0, 0.0)
        val holos = block.world.newHologram(
            holoLoc,
            true,
            "§aMáquina de ${type.display}",
            null,
            "§aDono: §b${ownerDisplay}",
            "§aQuantidade: §c${amount.formatEN()}",
            "§aCombustível: §e${combustivel.formatEN()}§7/§6${(upgradeCombustive.amount * amount).formatEN()}L"
        )

        holograms.addAll(holos)
    }

    fun delete() {
        holograms.forEach { stand ->
            stand.chunk.isForceLoaded = true
            if (!stand.chunk.isLoaded) {
                stand.chunk.load(true)
            }

            stand.remove()
        }
        holograms.clear()

        Main.instance.storage.remove("Machines.${block.world.name}_${block.x}_${block.y}_${block.z}")
        Main.instance.storage.saveConfig()
        MachineSystem.loadedMachines.remove(block)
    }

}