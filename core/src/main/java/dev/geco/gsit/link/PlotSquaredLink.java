package dev.geco.gsit.link;

import org.bukkit.*;
import org.bukkit.entity.*;

import com.plotsquared.core.*;
import com.plotsquared.core.plot.*;

import dev.geco.gsit.GSitMain;

public class PlotSquaredLink {

    private final GSitMain GPM;

    public PlotSquaredLink(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean canCreateSeat(Location Location, Player Player) {

        try {

            PlotAPI plapi = new PlotAPI();

            Plot plot = plapi.wrapPlayer(Player.getUniqueId()).getCurrentPlot();

            if(plot != null) {

                boolean c = GPM.getSpawnUtil().checkLocation(Location);

                if((plot.getArea() == null || !plot.getArea().isSpawnCustom()) && !c) return false;

                return (!GPM.getCManager().TRUSTED_REGION_ONLY || plot.isAdded(Player.getUniqueId())) && c;
            }

            return !GPM.getCManager().TRUSTED_REGION_ONLY && GPM.getSpawnUtil().checkLocation(Location);

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