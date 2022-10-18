package me.koddy.kmachines.`object`

import org.bukkit.Material

class MachineConfigData (
    var name: String,
    var display: String,
    var dropPrice: Int,
    var lore: MutableList<String>,
    var BlockType: Material,
    var DropType: Material
        )