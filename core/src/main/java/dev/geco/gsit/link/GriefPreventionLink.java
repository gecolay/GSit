package dev.geco.gsit.link;

import org.bukkit.*;
import org.bukkit.entity.*;

import me.ryanhamshire.GriefPrevention.*;

import dev.geco.gsit.GSitMain;

public class GriefPreventionLink {

    private final GSitMain GPM;

    public GriefPreventionLink(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean check(Location Location, Player Player) {
        try {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(Location, false, null);
            return claim == null || (!GPM.getCManager().TRUSTED_REGION_ONLY || claim.canSiege(Player));
        } catch (Throwable e) { e.printStackTrace(); }

        return true;
    }

}