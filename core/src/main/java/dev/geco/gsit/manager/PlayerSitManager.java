package dev.geco.gsit.manager;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.*;

public class PlayerSitManager {

    private final GSitMain GPM;

    public PlayerSitManager(GSitMain GPluginMain) {
        GPM = GPluginMain;
        seat_entity_count = GPM.getSVManager().isNewerOrVersion(20, 2) ? 1 : 2;
    }

    private int feature_used = 0;

    public int getFeatureUsedCount() { return feature_used; }

    public void resetFeatureUsedCount() { feature_used = 0; }

    private final int seat_entity_count;

    public int getSeatEntityCount() { return seat_entity_count; }

    public void clearSeats() { for(World world : Bukkit.getWorlds()) for(Entity entity : world.getEntities()) if(entity.getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity")) entity.remove(); }

    public boolean sitOnPlayer(Player Player, Player Target) {

        PrePlayerPlayerSitEvent preEvent = new PrePlayerPlayerSitEvent(Player, Target);

        Bukkit.getPluginManager().callEvent(preEvent);

        if(preEvent.isCancelled()) return false;

        if(!GPM.getEntityUtil().isPlayerSitLocationValid(Target)) return false;

        GPM.getEntityUtil().createPlayerSeatEntity(Target, Player);

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

        if(Entity.getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity")) Entity.remove();

        if(Entity instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerGetUpPlayerSitEvent((Player) Entity, Reason));

        return true;
    }

    private void removePassengers(Entity Entity) {

        for(Entity passenger : Entity.getPassengers()) {

            if(!passenger.getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity")) continue;

            removePassengers(passenger);

            passenger.remove();
        }
    }

    private void removeVehicles(Entity Entity) {

        Entity vehicle = Entity.getVehicle();

        if(vehicle == null) return;

        if(!vehicle.getScoreboardTags().contains(GPM.NAME + "_PlayerSeatEntity")) return;

        removeVehicles(vehicle);

        vehicle.remove();
    }

}