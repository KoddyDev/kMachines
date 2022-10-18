package me.koddy.kmachines.command

import com.mikael.mkutils.spigot.api.runCommand
import me.koddy.kmachines.Main
import me.koddy.kmachines.menus.DropsMenu
import net.eduard.api.lib.manager.CommandManager
import org.bukkit.entity.Player

class DropsCommand: CommandManager("drops") {
    init {
        usage = "/drops"
        permission = "kmachines.drops"
        permissionMessage = "§cSem permissão :c"

        register(MachineGiveCommand())

        this.command.setExecutor(this@DropsCommand)
    }

    override fun playerCommand(player: Player, args: Array<String>) {
        player.runCommand {
            DropsMenu.getMenu(player).open(player)
        }
    }
}