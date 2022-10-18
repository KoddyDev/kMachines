package me.koddy.kmachines.core

import com.mikael.mkutils.spigot.api.lib.MineItem
import me.koddy.kmachines.Main
import me.koddy.kmachines.`object`.CombustivelData
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

object CombustivelSystem {
    var combustiveis = mutableListOf<CombustivelData>()

    fun loadCombustiveis() {
        combustiveis.clear()

        for (combustivel in Main.instance.combustiveis.getSection("Combustiveis").keys) {
            val name = Main.instance.combustiveis.getString("Combustiveis.$combustivel.Name").replace("&", "§")
            val litros = Main.instance.combustiveis.getInt("Combustiveis.$combustivel.Litros")
            val lore = Main.instance.combustiveis.getStringList("Combustiveis.$combustivel.Lore")
            val infinity = Main.instance.combustiveis.getBoolean("Combustiveis.$combustivel.Infinity")
            val item = MineItem(Material.COAL)
            item.name(name)
            item.lore(*lore.toMutableList().map { it.replace("&", "§").replace("{litros}", litros.toString()) }.toTypedArray())

            val meta = item.itemMeta
            meta.persistentDataContainer.set(
                NamespacedKey(Main.instance, "combustivel"),
                PersistentDataType.STRING,
                "${name};${litros};${infinity};"
            )

            item.itemMeta = meta

            combustiveis.add(
                CombustivelData(
                    combustivel,
                    name,
                    lore.toMutableList(),
                    lore.map { it.replace("&", "§").replace("{litros}", litros.toString()) }.toMutableList(),
                    item,
                    litros,
                    infinity
                )
            )
        }
    }
}