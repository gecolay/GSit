package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

public class PassengerUtil {

    private final GSitMain GPM;

    public PassengerUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public long getVehicleAmount(Entity Entity) {

        long amount = 0;

        if(Entity.isInsideVehicle()) {

            Entity entity = Entity.getVehicle();

            if(entity instanceof Player) amount++;

            amount += getVehicleAmount(entity);
        }

        return amount;
    }

    public long getPassengerAmount(Entity Entity) {

        long amount = 0;

        for(Entity entity : Entity.getPassengers()) {

            if(entity instanceof Player) amount++;

            amount += getPassengerAmount(entity);
        }

        return amount;
    }

    public boolean isInPassengerList(Entity Entity, Entity Passenger) {

        List<Entity> passengers = Entity.getPassengers();

        if(passengers.contains(Passenger)) return true;

        for(Entity i : passengers) {

            boolean r = isInPassengerList(i, Passenger);

            if(r) return true;
        }

        return false;
    }

    public Entity getHighestEntity(Entity Entity) { return Entity.getPassengers().size() == 0 ? Entity : getHighestEntity(Entity.getPassengers().get(0)); }

    public boolean isNPC(Player P) { return !Bukkit.getOnlinePlayers().contains(P); }

}