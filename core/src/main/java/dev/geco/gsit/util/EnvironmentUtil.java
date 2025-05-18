package dev.geco.gsit.util;

import dev.geco.gsit.GSitMain;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnvironmentUtil {

    private final GSitMain gSitMain;

    public EnvironmentUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    public boolean isEntityInAllowedWorld(@NotNull Entity entity) {
        boolean allowed = !gSitMain.getConfigService().WORLDBLACKLIST.contains(entity.getWorld().getName());
        if(!gSitMain.getConfigService().WORLDWHITELIST.isEmpty() && !gSitMain.getConfigService().WORLDWHITELIST.contains(entity.getWorld().getName())) allowed = false;
        return allowed || gSitMain.getPermissionService().hasPermission(entity, "ByPass.World", "ByPass.*");
    }

    public boolean canUseInLocation(@NotNull Location location, @NotNull Player player, @NotNull String flag) {
        if(gSitMain.getPermissionService().hasPermission(player, "ByPass.Region", "ByPass.*")) return true;
        if(gSitMain.getPlotSquaredLink() != null) {
            if(flag.equalsIgnoreCase("sit")) {
                if(!gSitMain.getPlotSquaredLink().canUseSitInLocation(location, player)) return false;
            } else if(flag.equalsIgnoreCase("playersit")) {
                if(!gSitMain.getPlotSquaredLink().canUsePlayerSitInLocation(location, player)) return false;
            } else if(!gSitMain.getPlotSquaredLink().canUseInLocation(location, player)) return false;
        }
        if(gSitMain.getWorldGuardLink() != null && !gSitMain.getWorldGuardLink().canUseInLocation(location, player, flag)) return false;
        if(gSitMain.getGriefPreventionLink() != null && !gSitMain.getGriefPreventionLink().canUseInLocation(location, player)) return false;
        return true;
    }

}