package dev.geco.gsit.util;

import dev.geco.gsit.GSitMain;
import org.bukkit.entity.Entity;

public class EnvironmentUtil {

    private final GSitMain gSitMain;

    public EnvironmentUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    public boolean isEntityInAllowedWorld(Entity entity) {
        boolean allowed = !gSitMain.getConfigService().WORLDBLACKLIST.contains(entity.getWorld().getName());
        if(!gSitMain.getConfigService().WORLDWHITELIST.isEmpty() && !gSitMain.getConfigService().WORLDWHITELIST.contains(entity.getWorld().getName())) allowed = false;
        return allowed || gSitMain.getPermissionService().hasPermission(entity, "ByPass.World", "ByPass.*");
    }

}