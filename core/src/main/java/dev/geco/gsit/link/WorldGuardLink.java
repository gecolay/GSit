package dev.geco.gsit.link;

import java.util.*;

import org.bukkit.*;

import com.sk89q.worldguard.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.*;

import dev.geco.gsit.GSitMain;

public class WorldGuardLink {

    private final GSitMain GPM;
    private final HashMap<String, StateFlag> FLAGS = new HashMap<>(); {
        FLAGS.put("sit", new StateFlag("sit", true));
        FLAGS.put("playersit", new StateFlag("playersit", true));
        FLAGS.put("pose", new StateFlag("pose", true));
        FLAGS.put("crawl", new StateFlag("crawl", true));
    }

    public WorldGuardLink(GSitMain GPluginMain) { GPM = GPluginMain; }

    public StateFlag getFlag(String FlagName) { return FlagName != null ? FLAGS.getOrDefault(FlagName.toLowerCase(), null) : null; }

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

    public boolean checkFlag(Location Location, StateFlag Flag) {
        if(Flag == null) return true;
        try {
            return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(Location)).testState(null, Flag);
        } catch (Throwable e) { e.printStackTrace(); }
        return true;
    }

}