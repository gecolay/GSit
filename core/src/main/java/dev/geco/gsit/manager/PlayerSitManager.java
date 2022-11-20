package dev.geco.gsit.manager;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class PlayerSitManager {

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

        GPM.getSpawnUtil().createPlayerSeatEntity(Target, Player);

        if(GPM.getCManager().PS_SIT_MESSAGE) GPM.getMManager().sendActionBarMessage(Player, "Messages.action-playersit-info");

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

        removePassengers(Entity);

        removeVehicles(Entity);

        if(Entity.hasMetadata(GPM.NAME + "A")) {

            Entity.eject();

            Entity.remove();
        }

        if(Entity instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) Entity, Reason));

        return true;
    }

    private void removePassengers(Entity Entity) {

        for(Entity passenger : Entity.getPassengers()) {

            if(passenger.hasMetadata(GPM.NAME + "A")) {

                removePassengers(passenger);

                passenger.eject();

                passenger.remove();
            }
        }
    }

    private void removeVehicles(Entity Entity) {

        if(Entity.isInsideVehicle()) {

            Entity vehicle = Entity.getVehicle();

            if(vehicle.hasMetadata(GPM.NAME + "A")) {

                removeVehicles(vehicle);

                vehicle.eject();

                vehicle.remove();
            }
        }
    }

}