package dev.geco.gsit.cmd;

import org.jetbrains.annotations.NotNull;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import dev.geco.gsit.GSitMain;

public class GSitReloadCommand implements CommandExecutor {

    private final GSitMain GPM;

    public GSitReloadCommand(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, String[] a) {
        if(s instanceof Player || s instanceof ConsoleCommandSender || s instanceof RemoteConsoleCommandSender) {
            if(GPM.getPManager().hasPermission(s, GPM.NAME + "Reload")) {
                GPM.reload(s);
                GPM.getMManager().sendMessage(s, "Messages.command-reload");
            } else GPM.getMManager().sendMessage(s, "Messages.command-permission-error");
        } else GPM.getMManager().sendMessage(s, "Messages.command-sender-error");
        return true;
    }

}