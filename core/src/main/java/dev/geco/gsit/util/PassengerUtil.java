package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

public class PassengerUtil {

    public long getVehicleAmount(Entity Entity) {

        long amount = 0;

        Entity entity = Entity.getVehicle();

        if(entity == null) return amount;

        if(entity instanceof Player) amount++;

        return amount + getVehicleAmount(entity);
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

    public Entity getHighestEntity(Entity Entity) { return Entity == null || Entity.getPassengers().isEmpty() ? Entity : getHighestEntity(Entity.getPassengers().get(0)); }

    public Entity getBottomEntity(Entity Entity) { return Entity == null || Entity.getVehicle() == null ? Entity : getBottomEntity(Entity.getVehicle()); }

    public boolean isNPC(Player P) { return !Bukkit.getOnlinePlayers().contains(P); }

}