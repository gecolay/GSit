package dev.geco.gsit.link;

import org.bukkit.*;
import org.bukkit.entity.*;

import com.plotsquared.core.*;
import com.plotsquared.core.player.*;
import com.plotsquared.core.plot.*;

import dev.geco.gsit.GSitMain;

public class PlotSquaredLink {

    private final GSitMain GPM;

    public PlotSquaredLink(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean canCreateSeat(Location Location, Player Player) { return GPM.getEntityUtil().isLocationValid(Location) && canCreate(Location, Player); }

    public boolean canCreatePlayerSeat(Location Location, Player Player) { return GPM.getEntityUtil().isPlayerSitLocationValid(Player) && canCreate(Location, Player); }

    private boolean canCreate(Location Location, Player Player) {

        try {

            PlotAPI plotAPI = new PlotAPI();

            PlotPlayer<?> plotPlayer = plotAPI.wrapPlayer(Player.getUniqueId());
            if(plotPlayer == null) return !GPM.getCManager().TRUSTED_REGION_ONLY;

            com.plotsquared.core.location.Location location = com.plotsquared.core.location.Location.at(plotPlayer.getLocation().getWorld(), Location.getBlockX(), Location.getBlockY(), Location.getBlockZ());

            PlotArea plotArea = plotAPI.getPlotSquared().getPlotAreaManager().getApplicablePlotArea(location);
            if(plotArea == null) return !GPM.getCManager().TRUSTED_REGION_ONLY;

            Plot plot = plotArea.getOwnedPlot(location);
            if(plot == null) return !GPM.getCManager().TRUSTED_REGION_ONLY;

            return !plot.isDenied(Player.getUniqueId()) && (!GPM.getCManager().TRUSTED_REGION_ONLY || plot.isAdded(Player.getUniqueId()));
        } catch (Throwable e) { e.printStackTrace(); }

        return true;
    }

    public boolean isVersionSupported() {
        try {
            new PlotAPI();

            return true;
        } catch (Throwable ignored) { }
        return false;
    }

}