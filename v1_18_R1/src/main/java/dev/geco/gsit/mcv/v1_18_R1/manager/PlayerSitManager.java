package dev.geco.gsit.mcv.v1_18_R1.manager;

import org.bukkit.entity.Player;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class PlayerSitManager implements IPlayerSitManager {

    private final GSitMain GPM;

    public PlayerSitManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    public boolean sitOnPlayer(Player Player, Player Target) {
        Player.addPassenger(Target);
        return true;
    }

    public void ejectPassengers(Player Player, GetUpReason Reason) {
        Player.eject();
    }

}