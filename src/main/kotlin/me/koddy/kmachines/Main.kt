package me.koddy.kmachines

import com.mikael.mkutils.api.mkplugin.MKPlugin
import com.mikael.mkutils.api.mkplugin.MKPluginSystem
import me.koddy.kmachines.command.DropsCommand
import me.koddy.kmachines.command.MachinesCommand
import me.koddy.kmachines.core.CombustivelSystem
import me.koddy.kmachines.core.MachineSystem
import me.koddy.kmachines.listener.GeneralListener
import net.eduard.api.lib.config.Config
import net.eduard.api.lib.modules.BukkitTimeHandler
import net.eduard.api.lib.storage.StorageAPI
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class Main: JavaPlugin(), MKPlugin, BukkitTimeHandler {

    companion object {
        lateinit var instance: Main
    }

    lateinit var econ: Economy

    lateinit var storage: Config
    lateinit var combustiveis: Config
    lateinit var maquinas: Config

    override fun onEnable() {
        instance = this@Main
        val start = System.currentTimeMillis()

        if(!setupEconomy()) {
            log("§cVault não encontrado.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        log("Carregando diretorios...")

        loadConfigs()

        log("Carregando Maquinas e Combustiveis...")

        MachineSystem.loadMachines()
        CombustivelSystem.loadCombustiveis()

        log("Carregadas ${MachineSystem.loadedMachines.size} maquinas e ${CombustivelSystem.combustiveis.size} combustiveis")

        log("Carregando Sistemas...")

        MachinesCommand().registerCommand(this)
        DropsCommand().registerCommand(this)
        GeneralListener().registerListener(this)

        val end = System.currentTimeMillis() - start
        log("Plugin carregado (${end}ms)")

        MKPluginSystem.loadedMKPlugins.add(this@Main)
    }

    override fun onDisable() {
        log("Descarregando Sistemas...")

        MachineSystem.onDisable();

        log("Plugin descarregado!")

        MKPluginSystem.loadedMKPlugins.remove(this@Main)
    }

    private fun loadConfigs() {
        storage = Config(this@Main, "storage.yml")
        storage.saveConfig()

        combustiveis = Config(this@Main, "combustiveis.yml")
        combustiveis.saveConfig()

        maquinas = Config(this@Main, "machines.yml")
        maquinas.saveConfig()
        StorageAPI.updateReferences()

        combustiveis.add("Combustiveis.Gasolina.Name", "&fGasolina")
        combustiveis.add("Combustiveis.Gasolina.Lore", listOf("&fQuantidade: {litros}", ""))
        combustiveis.add("Combustiveis.Gasolina.Litros", 50)
        combustiveis.add("Combustiveis.Gasolina.Price", 1000)
        combustiveis.add("Combustiveis.Gasolina.Infinity", false)
        combustiveis.add("Combustiveis.Gasolina.buyInMenu", true)

        maquinas.add("Drops.Level1.Display", "1")
        maquinas.add("Drops.Level1.Amount", 10, "Quantidade de drops a ser gerado no nivel 1")
        maquinas.add("Drops.Level1.Delay", 13, "Quantidade de segundos de delay para gerar o drop.")
        maquinas.add("Drops.Level1.Price", 13, "Preço para upar para este nivel")

        maquinas.add("Drops.Level2.Display", "2")
        maquinas.add("Drops.Level2.Amount", 15)
        maquinas.add("Drops.Level2.Delay", 11)
        maquinas.add("Drops.Level2.Price", 13, "Preço para upar para este nivel")

        maquinas.add("Drops.Level3.Display", "3")
        maquinas.add("Drops.Level3.Amount", 20)
        maquinas.add("Drops.Level3.Delay", 9)
        maquinas.add("Drops.Level3.Price", 13, "Preço para upar para este nivel")

        maquinas.add("Drops.Level4.Display", "4")
        maquinas.add("Drops.Level4.Amount", 25)
        maquinas.add("Drops.Level4.Delay", 7)
        maquinas.add("Drops.Level4.Price", 13, "Preço para upar para este nivel")

        maquinas.add("Drops.Level5.Display", "5")
        maquinas.add("Drops.Level5.Amount", 30)
        maquinas.add("Drops.Level5.Delay", 5)
        maquinas.add("Drops.Level5.Price", 13, "Preço para upar para este nivel")


        maquinas.add("Combustivel.Level1.Display", "1")
        maquinas.add("Combustivel.Level1.Amount", 10, "Quantidade de capacidade de armazenar combustivel, neste caso é 10 litros")
        maquinas.add("Combustivel.Level1.Price", 13, "Preço para upar para este nivel")

        maquinas.add("Combustivel.Level2.Display", "2")
        maquinas.add("Combustivel.Level2.Amount", 20)
        maquinas.add("Combustivel.Level2.Price", 13, "Preço para upar para este nivel")

        maquinas.add("Combustivel.Level3.Display", "3")
        maquinas.add("Combustivel.Level3.Amount", 30)
        maquinas.add("Combustivel.Level3.Price", 13, "Preço para upar para este nivel")

        maquinas.add("Combustivel.Level4.Display", "4")
        maquinas.add("Combustivel.Level4.Amount", 40)
        maquinas.add("Combustivel.Level4.Price", 13, "Preço para upar para este nivel")

        maquinas.add("Combustivel.Level5.Display", "5")
        maquinas.add("Combustivel.Level5.Amount", 50)
        maquinas.add("Combustivel.Level5.Price", 13, "Preço para upar para este nivel")

        maquinas.add("Maquinas.Carvao.Name", "Carvão", "Não insira cores no nome, apenas no display e lore.")
        maquinas.add("Maquinas.Carvao.Display", "&0Carvão")
        maquinas.add("Maquinas.Carvao.Lore", listOf("&fEsta maquina gerará carvão", "&fPreço: &e10000"))
        maquinas.add("Maquinas.Carvao.BlockType", Material.COAL_BLOCK.name, "Insira apenas nomes de blocos. Você pode encontrar os nomes em: https://helpch.at/docs/1.8/org/bukkit/Material.html")
        maquinas.add("Maquinas.Carvao.DropType", Material.COAL.name, "Insira apenas nomes de itens que não são blocos")
        maquinas.add("Maquinas.Carvao.DropPrice", 1000, "Insira apenas nomes de itens que não são blocos")

        combustiveis.saveConfig()
        maquinas.saveConfig()
    }

    fun log(msg: String) {
        Bukkit.getConsoleSender().sendMessage("§a[$systemName] §f$msg")
    }

    private fun setupEconomy(): Boolean {
        if (!server.pluginManager.getPlugin("Vault")!!.isEnabled) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        econ = rsp.provider

        return true
    }

    override val isFree: Boolean
        get() = true

    override fun getPlugin(): Any {
        return this
    }

    override fun getPluginFolder(): File {
        return this.dataFolder
    }

    override fun getSystemName(): String {
        return "kMachines"
    }

    override fun getPluginConnected(): Plugin {
        return this
    }
}