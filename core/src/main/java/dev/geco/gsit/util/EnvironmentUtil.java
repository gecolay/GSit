package dev.geco.gsit.util;

import dev.geco.gsit.GSitMain;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnvironmentUtil {

    private final GSitMain gSitMain;
    private final boolean worldWhitelistEnabled;
    private final boolean bypassPermissionEnabled;

    public EnvironmentUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        var config = gSitMain.getConfigService();
        this.worldWhitelistEnabled = !config.WORLDWHITELIST.isEmpty();
        this.bypassPermissionEnabled = config.PERMISSION_SERVICE_ENABLED;
    }

    public boolean isEntityInAllowedWorld(@NotNull Entity entity) {
        String worldName = entity.getWorld().getName();
        var config = gSitMain.getConfigService();
        
        // Bypass permission check first (cheapest)
        if (bypassPermissionEnabled && 
            gSitMain.getPermissionService().hasPermission(entity, "ByPass.World", "ByPass.*")) {
            return true;
        }
        
        // World whitelist has priority if enabled
        if (worldWhitelistEnabled) {
            return config.WORLDWHITELIST.contains(worldName);
        }
        
        // Fallback to blacklist check
        return !config.WORLDBLACKLIST.contains(worldName);
    }

    public boolean canUseInLocation(@NotNull Location location, @NotNull Player player, @NotNull String flag) {
        // Bypass permission check first (cheapest)
        if (bypassPermissionEnabled && 
            gSitMain.getPermissionService().hasPermission(player, "ByPass.Region", "ByPass.*")) {
            return true;
        }
        
        // Check region protections in optimal order
        if (gSitMain.getPlotSquaredLink() != null && !checkPlotSquared(location, player, flag)) {
            return false;
        }
        if (gSitMain.getWorldGuardLink() != null && !gSitMain.getWorldGuardLink().canUseInLocation(location, player, flag)) {
            return false;
        }
        if (gSitMain.getGriefPreventionLink() != null && !gSitMain.getGriefPreventionLink().canUseInLocation(location, player)) {
            return false;
        }
        
        return true;
    }
    
    private boolean checkPlotSquared(Location location, Player player, String flag) {
        return switch (flag.toLowerCase()) {
            case "sit" -> gSitMain.getPlotSquaredLink().canUseSitInLocation(location, player);
            case "playersit" -> gSitMain.getPlotSquaredLink().canUsePlayerSitInLocation(location, player);
            default -> gSitMain.getPlotSquaredLink().canUseInLocation(location, player);
        };
    }
}
