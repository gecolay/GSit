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

        PrePlayerPlayerSitEvent preEvent = new PrePlayerPlayerSitEvent(Player, Target);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        if(!GPM.getSpawnUtil().checkPlayerLocation(Target)) return false;

        Entity playerSeatEntity = GPM.getSpawnUtil().createPlayerSeatEntity(Target, Player);

        if(GPM.getCManager().PS_SIT_MESSAGE) GPM.getMManager().sendActionBarMessage(Player, "Messages.action-playersit-info");

        playerSeatEntity.setMetadata(GPM.NAME + "A", new FixedMetadataValue(GPM, Player));

        feature_used++;

        Bukkit.getPluginManager().callEvent(new PlayerPlayerSitEvent(Player, Target));

        return true;
    }

    public boolean stopPlayerSit(Entity Entity, GetUpReason Reason) {

        if(Entity instanceof Player) {

            PrePlayerGetUpPlayerSitEvent preEvent = new PrePlayerGetUpPlayerSitEvent((Player) Entity, Reason);

            Bukkit.getPluginManager().callEvent(preEvent);

            if(preEvent.isCancelled()) return false;

        }

        if(Entity.hasMetadata(GPM.NAME + "A")) {

            Entity.eject();

            Entity.remove();
        }

        for(Entity passenger : Entity.getPassengers()) {

            if(passenger.hasMetadata(GPM.NAME + "A")) {

                passenger.eject();

                passenger.remove();
            }
        }

        if(Entity.isInsideVehicle()) {

            Entity vehicle = Entity.getVehicle();

            if(vehicle.hasMetadata(GPM.NAME + "A")) {

                vehicle.eject();

                vehicle.remove();
            }
        }

        if(Entity instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) Entity, Reason));

        return true;
    }

}