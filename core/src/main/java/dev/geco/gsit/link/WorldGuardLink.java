package dev.geco.gsit.link;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.geco.gsit.GSitMain;
import dev.geco.gsit.link.worldguard.RegionFlagHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class WorldGuardLink {

    public static final StateFlag SIT_FLAG = new StateFlag("sit", true);
    public static final StateFlag PLAYERSIT_FLAG = new StateFlag("playersit", true);
    public static final StateFlag POSE_FLAG = new StateFlag("pose", true);
    public static final StateFlag CRAWL_FLAG = new StateFlag("crawl", true);

    public void registerFlags() {
        HashMap<String, StateFlag> flags = new HashMap<>();
        flags.put(SIT_FLAG.getName(), SIT_FLAG);
        flags.put(PLAYERSIT_FLAG.getName(), PLAYERSIT_FLAG);
        flags.put(POSE_FLAG.getName(), POSE_FLAG);
        flags.put(CRAWL_FLAG.getName(), CRAWL_FLAG);
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        for(Map.Entry<String, StateFlag> flag : flags.entrySet()) {
            try {
                flagRegistry.register(flag.getValue());
            } catch(Throwable ignored) { }
        }
    }

    public void registerFlagHandlers() {
        WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(RegionFlagHandler.FACTORY, null);
    }

    public void unregisterFlagHandlers() {
        WorldGuard.getInstance().getPlatform().getSessionManager().unregisterHandler(RegionFlagHandler.FACTORY);
    }

    public boolean canUseInLocation(Location location, Player player, String flag) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            if(container.get(BukkitAdapter.adapt(location.getWorld())) == null) return true;
            RegionQuery regionQuery = container.createQuery();
            com.sk89q.worldedit.util.Location regionLocation = BukkitAdapter.adapt(location);
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            // If the player can't ride an entity in the region, we can't use sit anyway
            if(!flag.equalsIgnoreCase("crawl") && !regionQuery.testBuild(regionLocation, localPlayer, Flags.RIDE, Flags.INTERACT)) return false;
            FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
            if(!(flagRegistry.get(flag) instanceof StateFlag stateFlag)) return true;
            return regionQuery.testState(regionLocation, localPlayer, stateFlag);
        } catch(Throwable e) { GSitMain.getInstance().getLogger().log(Level.SEVERE, "Could not check WorldGuard location!", e); }
        return true;
    }

}