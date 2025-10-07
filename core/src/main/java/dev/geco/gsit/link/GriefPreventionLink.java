package dev.geco.gsit.link;

import dev.geco.gsit.GSitMain;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class GriefPreventionLink {

    private final GSitMain gSitMain;

    public GriefPreventionLink(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    public boolean canUseInLocation(Location location, Player player) {
        try {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            return claim == null || claim.checkPermission(player, ClaimPermission.Edit, null) != null;
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not check GriefPrevention location!", e); }
        return true;
    }

}