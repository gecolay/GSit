package dev.geco.gsit.cmd;

import dev.geco.gsit.GSitMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GSitReloadCommand implements CommandExecutor {

    private final GSitMain gSitMain;

    public GSitReloadCommand(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
            return true;
        }

        if(!gSitMain.getPermissionService().hasPermission(sender, "Reload")) {
            gSitMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
            return true;
        }

        gSitMain.reload(sender);

        gSitMain.getMessageService().sendMessage(sender, "Plugin.plugin-reload");
        return true;
    }

}