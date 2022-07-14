package dev.geco.gsit.manager;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

public class PManager {

    private final GSitMain GPM;

    public PManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean hasPermission(CommandSender Sender, String... Permissions) {

        if(!(Sender instanceof Player)) return true;

        for(String i : Permissions) {

            if(Sender.isPermissionSet(GPM.NAME + "." + i)) return Sender.hasPermission(GPM.NAME + "." + i);

            if(Sender.hasPermission(GPM.NAME + "." + i)) return true;
        }

        return Sender.hasPermission(GPM.NAME + ".*");
    }

}