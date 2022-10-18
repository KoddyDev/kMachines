package me.koddy.kmachines.`object`

import com.mikael.mkutils.spigot.api.lib.MineItem
import me.koddy.kmachines.Main

class CombustivelData (
    var key: String,
    var name: String,
    var oriLore: MutableList<String>,
    var lore: MutableList<String>,
    var item: MineItem,
    var litros: Int,
    var infinity: Boolean
    ) {
    fun saveToDisk() {
        Main.instance.combustiveis["Combustiveis.${key}.Name"] = name.replace("§", "&")
        Main.instance.combustiveis["Combustiveis.${key}.Lore"] = lore.map { it.replace("§", "&") }
        Main.instance.combustiveis["Combustiveis.${key}.Litros"] = litros
        Main.instance.combustiveis["Combustiveis.${key}.Infinity"] = infinity

        Main.instance.storage.saveConfig()
    }
}