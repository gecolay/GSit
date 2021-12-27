package dev.geco.gsit.manager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.geco.gsit.GSitMain;

public class PManager {

    private final GSitMain GPM;

    public PManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean hasNormalPermission(CommandSender S, String... P) {
        if(!(S instanceof Player) || !GPM.getCManager().CHECK_FEATURE_PERMISSIONS) return true;
        return hasPermission(S, P);
    }

    public boolean hasPermission(CommandSender S, String... P) {
        if(!(S instanceof Player)) return true;
        for(String i : P) {
            if(S.isPermissionSet(GPM.NAME + "." + i)) return S.hasPermission(GPM.NAME + "." + i);
            if(S.hasPermission(GPM.NAME + "." + i)) return true;
        }
        return S.hasPermission(GPM.NAME + ".*");
    }

}