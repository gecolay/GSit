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

            PlotAPI plapi = new PlotAPI();

            com.plotsquared.core.location.Location ploc = com.plotsquared.core.location.Location.at(plapi.wrapPlayer(Player.getUniqueId()).getLocation().getWorld(), Location.getBlockX(), Location.getBlockY(), Location.getBlockZ());

            PlotArea plotarea = plapi.getPlotSquared().getPlotAreaManager().getApplicablePlotArea(ploc);

            if(plotarea == null) return !GPM.getCManager().TRUSTED_REGION_ONLY;

            if(!plotarea.isSpawnCustom()) return false;

            Plot plot = plotarea.getOwnedPlot(ploc);

            if(plot == null) return !GPM.getCManager().TRUSTED_REGION_ONLY;

            return !GPM.getCManager().TRUSTED_REGION_ONLY || plot.isAdded(Player.getUniqueId());

        } catch (Exception | Error e) {
            e.printStackTrace();
        }

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