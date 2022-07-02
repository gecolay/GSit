package dev.geco.gsit.link;

import org.bukkit.*;
import org.bukkit.entity.*;

import me.ryanhamshire.GriefPrevention.*;

import dev.geco.gsit.GSitMain;

public class GrPrLink {

    private final GSitMain GPM;

    public GrPrLink(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean check(Location Location, Player Player) {

        try {

            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(Location, false, null);

            return claim == null || claim.canSiege(Player);

        } catch (Exception | Error e) {
            e.printStackTrace();
        }

        return true;
    }

}