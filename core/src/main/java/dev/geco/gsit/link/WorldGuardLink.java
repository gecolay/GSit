package dev.geco.gsit.link;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import dev.geco.gsit.GSitMain;
import dev.geco.gsit.link.worldguard.RegionFlagHandler;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class WorldGuardLink {

    public static final StateFlag SIT_FLAG = new StateFlag("sit", true);
    public static final StateFlag PLAYERSIT_FLAG = new StateFlag("playersit", true);
    public static final StateFlag POSE_FLAG = new StateFlag("pose", true);
    public static final StateFlag CRAWL_FLAG = new StateFlag("crawl", true);

    private final HashMap<String, StateFlag> FLAGS = new HashMap<>(); {
        FLAGS.put(SIT_FLAG.getName(), SIT_FLAG);
        FLAGS.put(PLAYERSIT_FLAG.getName(), PLAYERSIT_FLAG);
        FLAGS.put(POSE_FLAG.getName(), POSE_FLAG);
        FLAGS.put(CRAWL_FLAG.getName(), CRAWL_FLAG);
    }

    public StateFlag getFlag(String flagName) { return flagName != null ? FLAGS.getOrDefault(flagName.toLowerCase(), null) : null; }

    public void registerFlags() {
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        for(Map.Entry<String, StateFlag> flag : FLAGS.entrySet()) {
            try {
                flagRegistry.register(flag.getValue());
            } catch(FlagConflictException | IllegalStateException e) {
                Flag<?> registeredFlag = flagRegistry.get(flag.getKey());
                if(registeredFlag instanceof StateFlag) FLAGS.put(flag.getKey(), (StateFlag) registeredFlag);
            }
        }
    }

    public void registerFlagHandlers() {
        WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(RegionFlagHandler.FACTORY, null);
    }

    public void unregisterFlagHandlers() {
        WorldGuard.getInstance().getPlatform().getSessionManager().unregisterHandler(RegionFlagHandler.FACTORY);
    }

    public boolean canUseInLocation(Location location, StateFlag flag) {
        if(flag == null) return true;
        try {
            return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(location)).testState(null, flag);
        } catch(Throwable e) { GSitMain.getInstance().getLogger().log(Level.SEVERE, "Could not check WorldGuard location!", e); }
        return true;
    }

}