package dev.geco.gsit.link.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import dev.geco.gsit.GSitMain;
import dev.geco.gsit.link.WorldGuardLink;
import dev.geco.gsit.model.StopReason;
import dev.geco.gsit.model.Crawl;

import java.util.Set;

public class RegionFlagHandler extends Handler {

    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<RegionFlagHandler> {
        @Override
        public RegionFlagHandler create(Session session) {
            return new RegionFlagHandler(session);
        }
    }

    private final GSitMain gSitMain;
    private StateFlag.State currentPlayerSitFlagState = null;
    private StateFlag.State currentCrawlFlagState = null;

    public RegionFlagHandler(Session session) {
        super(session);
        gSitMain = GSitMain.getInstance();
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        StateFlag.State playerSitFlagState = toSet.queryState(player, WorldGuardLink.PLAYERSIT_FLAG);
        if(playerSitFlagState != currentPlayerSitFlagState) {
            currentPlayerSitFlagState = playerSitFlagState;
            handlePlayerSitFlagChange(player);
        }
        StateFlag.State crawlFlagState = toSet.queryState(player, WorldGuardLink.CRAWL_FLAG);
        if(crawlFlagState != currentCrawlFlagState) {
            currentCrawlFlagState = crawlFlagState;
            handleCrawlFlagChange(player);
        }
        return true;
    }

    private void handlePlayerSitFlagChange(LocalPlayer localPlayer) {
        if(currentPlayerSitFlagState != StateFlag.State.DENY) return;
        gSitMain.getPlayerSitService().stopPlayerSit(BukkitAdapter.adapt(localPlayer), StopReason.REGION);
    }

    private void handleCrawlFlagChange(LocalPlayer localPlayer) {
        if(currentCrawlFlagState != StateFlag.State.DENY) return;
        Crawl crawl = gSitMain.getCrawlService().getCrawlByPlayer(BukkitAdapter.adapt(localPlayer));
        if(crawl == null) return;
        gSitMain.getCrawlService().stopCrawl(crawl, StopReason.REGION);
    }

}
