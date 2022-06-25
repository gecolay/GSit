package dev.geco.gsit.manager;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class PlayerSitManager implements IPlayerSitManager {

    private final GSitMain GPM;

    public PlayerSitManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    public boolean sitOnPlayer(Player Player, Player Target) {

        PrePlayerPlayerSitEvent pplapse = new PrePlayerPlayerSitEvent(Player, Target);

        Bukkit.getPluginManager().callEvent(pplapse);

        if(pplapse.isCancelled()) return false;

        if(!GPM.getSpawnUtil().checkPlayerLocation(Target)) return false;

        Entity sa = GPM.getSpawnUtil().createPlayerSeatEntity(Target, Player);

        if(GPM.getCManager().PS_SIT_MESSAGE) GPM.getMManager().sendActionBarMessage(Player, "Messages.action-playersit-info");

        sa.setMetadata(GPM.NAME + "A", new FixedMetadataValue(GPM, Player));

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerPlayerSitEvent(Player, Target));

        return true;
    }

    public boolean stopPlayerSit(Entity Entity, GetUpReason Reason) {

        if(Entity instanceof Player) {

            PrePlayerGetUpPlayerSitEvent pplagupse = new PrePlayerGetUpPlayerSitEvent((Player) Entity, Reason);

            Bukkit.getPluginManager().callEvent(pplagupse);

            if(pplagupse.isCancelled()) return false;

        }

        if(Entity.hasMetadata(GPM.NAME + "A")) {
            Entity.eject();
            Entity.remove();
        }

        for(Entity e : Entity.getPassengers()) {
            if(e.hasMetadata(GPM.NAME + "A")) {
                e.eject();
                e.remove();
            }
        }

        if(Entity.isInsideVehicle()) {
            Entity e = Entity.getVehicle();
            if(e.hasMetadata(GPM.NAME + "A")) {
                e.eject();
                e.remove();
            }
        }

        if(Entity instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) Entity, Reason));

        return true;
    }

}