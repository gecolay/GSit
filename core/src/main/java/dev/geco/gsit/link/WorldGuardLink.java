package dev.geco.gsit.link;

import org.bukkit.*;

import com.sk89q.worldguard.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.*;

import dev.geco.gsit.GSitMain;

public class WorldGuardLink {

    private final GSitMain GPM;

    public WorldGuardLink(GSitMain GPluginMain) { GPM = GPluginMain; }

    public StateFlag SIT_FLAG;
    public StateFlag PLAYERSIT_FLAG;
    public StateFlag POSE_FLAG;
    public StateFlag CRAWL_FLAG;
    public StateFlag EMOTE_FLAG;

    public void registerFlags() {

        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();

        try {

            StateFlag stateFlag = new StateFlag("sit", true);
            flagRegistry.register(stateFlag);
            SIT_FLAG = stateFlag;
        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> flag = flagRegistry.get("sit");
            if(flag instanceof StateFlag) SIT_FLAG = (StateFlag) flag;
        }

        try {

            StateFlag stateFlag = new StateFlag("playersit", true);
            flagRegistry.register(stateFlag);
            PLAYERSIT_FLAG = stateFlag;
        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> flag = flagRegistry.get("playersit");
            if(flag instanceof StateFlag) PLAYERSIT_FLAG = (StateFlag) flag;
        }

        try {

            StateFlag stateFlag = new StateFlag("pose", true);
            flagRegistry.register(stateFlag);
            POSE_FLAG = stateFlag;
        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> flag = flagRegistry.get("pose");
            if(flag instanceof StateFlag) POSE_FLAG = (StateFlag) flag;
        }

        try {

            StateFlag stateFlag = new StateFlag("crawl", true);
            flagRegistry.register(stateFlag);
            CRAWL_FLAG = stateFlag;
        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> flag = flagRegistry.get("crawl");
            if(flag instanceof StateFlag) CRAWL_FLAG = (StateFlag) flag;
        }

        try {

            StateFlag stateFlag = new StateFlag("emote", true);
            flagRegistry.register(stateFlag);
            EMOTE_FLAG = stateFlag;
        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> flag = flagRegistry.get("emote");
            if(flag instanceof StateFlag) EMOTE_FLAG = (StateFlag) flag;
        }

    }

    public boolean checkFlag(Location Location, StateFlag Flag) {

        if(Flag == null) return true;

        try {

            return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(Location)).testState(null, Flag);
        } catch (Exception | Error e) { e.printStackTrace(); }

        return true;
    }

}