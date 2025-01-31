package dev.geco.gsit.service;

import dev.geco.gsit.GSitMain;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PermissionService {

    public boolean hasPermission(Permissible permissible, String... permissions) {
        if(!(permissible instanceof Player)) return true;
        for(String permission : permissions) {
            if(permissible.isPermissionSet(GSitMain.NAME + "." + permission)) return permissible.hasPermission(GSitMain.NAME + "." + permission);
            if(permissible.hasPermission(GSitMain.NAME + "." + permission)) return true;
        }
        return permissible.hasPermission(GSitMain.NAME + ".*");
    }

}