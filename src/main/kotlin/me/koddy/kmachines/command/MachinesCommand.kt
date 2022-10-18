package me.koddy.kmachines.command

import com.mikael.mkutils.spigot.api.runCommand
import me.koddy.kmachines.Main
import net.eduard.api.lib.manager.CommandManager
import org.bukkit.entity.Player

class MachinesCommand: CommandManager("maquinas") {
    init {
        usage = "/maquinas"
        permission = "kmachines.maquinas"
        permissionMessage = "§a${Main.instance.systemName} developed by KoddyDev"

        register(MachineGiveCommand())

        this.command.setExecutor(this@MachinesCommand)
    }

    override fun playerCommand(player: Player, args: Array<String>) {
        player.runCommand {
            player.sendMessage(permissionMessage)
        }
    }
}