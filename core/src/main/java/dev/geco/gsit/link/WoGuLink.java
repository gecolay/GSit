package dev.geco.gsit.link;

import org.bukkit.*;

import com.sk89q.worldguard.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.*;

import dev.geco.gsit.GSitMain;

public class WoGuLink {

    private final GSitMain GPM;

    public WoGuLink(GSitMain GPluginMain) { GPM = GPluginMain; }

    public StateFlag SIT_FLAG;
    public StateFlag PLAYERSIT_FLAG;
    public StateFlag POSE_FLAG;
    public StateFlag CRAWL_FLAG;
    public StateFlag EMOTE_FLAG;

    public void registerFlags() {

        FlagRegistry fr = WorldGuard.getInstance().getFlagRegistry();

        try {

            StateFlag sf = new StateFlag("sit", true);
            fr.register(sf);
            SIT_FLAG = sf;

        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> sf = fr.get("sit");
            if(sf instanceof StateFlag) {
                SIT_FLAG = (StateFlag) sf;
            }
        }

        try {

            StateFlag sf = new StateFlag("playersit", true);
            fr.register(sf);
            PLAYERSIT_FLAG = sf;

        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> sf = fr.get("playersit");
            if(sf instanceof StateFlag) {
                PLAYERSIT_FLAG = (StateFlag) sf;
            }
        }

        try {

            StateFlag sf = new StateFlag("pose", true);
            fr.register(sf);
            POSE_FLAG = sf;

        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> sf = fr.get("pose");
            if(sf instanceof StateFlag) {
                POSE_FLAG = (StateFlag) sf;
            }
        }

        try {

            StateFlag sf = new StateFlag("crawl", true);
            fr.register(sf);
            CRAWL_FLAG = sf;

        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> sf = fr.get("crawl");
            if(sf instanceof StateFlag) {
                CRAWL_FLAG = (StateFlag) sf;
            }
        }

        try {

            StateFlag sf = new StateFlag("emote", true);
            fr.register(sf);
            EMOTE_FLAG = sf;

        } catch (FlagConflictException | IllegalStateException e) {

            Flag<?> sf = fr.get("emote");
            if(sf instanceof StateFlag) {
                EMOTE_FLAG = (StateFlag) sf;
            }
        }

    }

    public boolean checkFlag(Location L, StateFlag Flag) {

        if(Flag == null) return true;

        try {

            return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(L)).testState(null, Flag);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}