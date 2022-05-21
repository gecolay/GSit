package dev.geco.gsit.link;

import org.bukkit.entity.Player;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.plot.Plot;

import dev.geco.gsit.GSitMain;

public class PlSqLink {

    private final GSitMain GPM;

    public PlSqLink(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean canCreateSeat(org.bukkit.Location L, Player P) {

        PlotAPI plapi = new PlotAPI();

        Plot plot = plapi.wrapPlayer(P.getUniqueId()).getCurrentPlot();

        if(plot != null) {

            boolean c = GPM.getSpawnUtil().checkLocation(L);

            if((plot.getArea() == null || !plot.getArea().isSpawnCustom()) && !c) return false;

            return (!GPM.getCManager().REST_TEAM_PLOTS_ONLY || plot.isAdded(P.getUniqueId())) && c;
        }

        return !GPM.getCManager().REST_TEAM_PLOTS_ONLY && GPM.getSpawnUtil().checkLocation(L);
    }

    public boolean isVersionSupported() {
        try {
            new PlotAPI();
            return true;
        } catch(Exception | Error ignored) { }
        return false;
    }

}