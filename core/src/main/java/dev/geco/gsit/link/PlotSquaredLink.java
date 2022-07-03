package dev.geco.gsit.link;

import org.bukkit.*;
import org.bukkit.entity.*;

import com.plotsquared.core.*;
import com.plotsquared.core.plot.*;

import dev.geco.gsit.GSitMain;

public class PlotSquaredLink {

    private final GSitMain GPM;

    public PlotSquaredLink(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean canCreateSeat(Location Location, Player Player) { return canCreate(Location, Player, GPM.getSpawnUtil().checkLocation(Location)); }

    public boolean canCreatePlayerSeat(Location Location, Player Player) { return canCreate(Location, Player, GPM.getSpawnUtil().checkPlayerLocation(Player)); }

    private boolean canCreate(Location Location, Player Player, boolean CheckLocation) {

        if(!CheckLocation) return false;

        try {

            PlotAPI plotAPI = new PlotAPI();

            com.plotsquared.core.location.Location location = com.plotsquared.core.location.Location.at(plotAPI.wrapPlayer(Player.getUniqueId()).getLocation().getWorld(), Location.getBlockX(), Location.getBlockY(), Location.getBlockZ());

            PlotArea plotArea = plotAPI.getPlotSquared().getPlotAreaManager().getApplicablePlotArea(location);

            if(plotArea == null) return !GPM.getCManager().TRUSTED_REGION_ONLY;

            Plot plot = plotArea.getOwnedPlot(location);

            if(plot == null) return !GPM.getCManager().TRUSTED_REGION_ONLY;

            return !GPM.getCManager().TRUSTED_REGION_ONLY || plot.isAdded(Player.getUniqueId());

        } catch (Exception | Error e) { e.printStackTrace(); }

        return true;
    }

    public boolean isVersionSupported() {

        try {

            new PlotAPI();

            return true;
        } catch(Exception | Error ignored) { }

        return false;
    }

}