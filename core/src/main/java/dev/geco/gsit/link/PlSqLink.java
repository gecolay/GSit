package dev.geco.gsit.link;

import dev.geco.gsit.manager.NMSManager;
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

            if(!plot.getArea().isSpawnCustom() && !(GPM.getCManager().DEV && NMSManager.isNewerOrVersion(17, 0))) return false;

            return !GPM.getCManager().REST_TEAM_PLOTS_ONLY || plot.isAdded(P.getUniqueId());

        }

        return !GPM.getCManager().REST_TEAM_PLOTS_ONLY;

    }

    public boolean isVersionSupported() {
        try {
            new PlotAPI();
            return true;
        } catch(Exception | Error e) { }
        return false;
    }

}