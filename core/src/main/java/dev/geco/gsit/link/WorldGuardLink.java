package dev.geco.gsit.link;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class WorldGuardLink {

    private static final HashMap<String, StateFlag> FLAGS = new HashMap<>(); {
        FLAGS.put("sit", new StateFlag("sit", true));
        FLAGS.put("playersit", new StateFlag("playersit", true));
        FLAGS.put("pose", new StateFlag("pose", true));
        FLAGS.put("crawl", new StateFlag("crawl", true));
    }

    public StateFlag getFlag(String flagName) { return flagName != null ? FLAGS.getOrDefault(flagName.toLowerCase(), null) : null; }

    public void registerFlags() {
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        for(Map.Entry<String, StateFlag> flag : FLAGS.entrySet()) {
            try {
                flagRegistry.register(flag.getValue());
            } catch (FlagConflictException | IllegalStateException e) {
                Flag<?> registeredFlag = flagRegistry.get(flag.getKey());
                if(registeredFlag instanceof StateFlag) FLAGS.put(flag.getKey(), (StateFlag) registeredFlag);
            }
        }
    }

    public boolean canUseInLocation(Location location, StateFlag flag) {
        if(flag == null) return true;
        try {
            return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(location)).testState(null, flag);
        } catch (Throwable e) { e.printStackTrace(); }
        return true;
    }

}