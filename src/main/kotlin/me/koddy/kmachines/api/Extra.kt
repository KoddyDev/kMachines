package me.koddy.kmachines.api

import me.koddy.kmachines.Main
import me.koddy.kmachines.core.MachineSystem
import me.koddy.kmachines.`object`.MachineData
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

val Location.nearbyBlocks: List<Block>
    get() {
        val blocks = mutableListOf<Block>()

        for (nX in (this.x.toInt()-3)..(this.x + 3).toInt()) {
            for (nY in (this.y.toInt()-3)..(this.y + 3).toInt()) {
                for (nZ in (this.z.toInt()-3)..(this.z + 3).toInt()) {
                    blocks.add(this.world.getBlockAt(nX, nY, nZ))
                }
            }
        }

        blocks.removeIf {
            Material.AIR == it.type
        }

        return blocks
    }

val Block.machine: MachineData?
    get() = MachineSystem.loadedMachines[this]

val ItemStack.isMachine: Boolean
    get() = this.itemMeta.persistentDataContainer.has(
        NamespacedKey(Main.instance, "machine"),
        PersistentDataType.STRING
    )