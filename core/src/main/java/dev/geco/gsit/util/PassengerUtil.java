package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.entity.*;

public class PassengerUtil {

    public long getVehicleAmount(Entity Entity) {
        long amount = 0;
        Entity vehicle = Entity.getVehicle();
        if(vehicle == null) return amount;
        if(vehicle instanceof Player) amount++;
        return amount + getVehicleAmount(vehicle);
    }

    public long getPassengerAmount(Entity Entity) {
        long amount = 0;
        for(Entity passenger : Entity.getPassengers()) {
            if(passenger instanceof Player) amount++;
            amount += getPassengerAmount(passenger);
        }
        return amount;
    }

    public boolean isInPassengerList(Entity Entity, Entity Passenger) {
        List<Entity> passengers = Entity.getPassengers();
        if(passengers.contains(Passenger)) return true;
        for(Entity passenger : passengers) if(isInPassengerList(passenger, Passenger)) return true;
        return false;
    }

    public Entity getHighestEntity(Entity Entity) { return Entity == null || Entity.getPassengers().isEmpty() ? Entity : getHighestEntity(Entity.getPassengers().get(0)); }

    public Entity getBottomEntity(Entity Entity) { return Entity == null || Entity.getVehicle() == null ? Entity : getBottomEntity(Entity.getVehicle()); }

}