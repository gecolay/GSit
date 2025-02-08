package dev.geco.gsit.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class PassengerUtil {

    public long getEntityVehicleCount(Entity entity) {
        long vehicleCount = 0;
        Entity currentVehicle = entity.getVehicle();
        if(currentVehicle == null) return vehicleCount;
        if(currentVehicle instanceof Player) vehicleCount++;
        return vehicleCount + getEntityVehicleCount(currentVehicle);
    }

    public long getEntityPassengerCount(Entity entity) {
        long passengerCount = 0;
        for(Entity currentPassenger : entity.getPassengers()) {
            if(currentPassenger instanceof Player) passengerCount++;
            passengerCount += getEntityPassengerCount(currentPassenger);
        }
        return passengerCount;
    }

    public boolean isEntityInEntityPassengerList(Entity entity, Entity passenger) {
        List<Entity> currentPassengers = entity.getPassengers();
        if(currentPassengers.contains(passenger)) return true;
        for(Entity currentPassenger : currentPassengers) if(isEntityInEntityPassengerList(currentPassenger, passenger)) return true;
        return false;
    }

    public Entity getTopEntityPassenger(Entity entity) {
        if(entity == null || entity.getPassengers().isEmpty()) return entity;
        return getTopEntityPassenger(entity.getPassengers().get(0));
    }

    public Entity getBottomEntityVehicle(Entity entity) {
        if(entity == null || entity.getVehicle() == null) return entity;
        return getBottomEntityVehicle(entity.getVehicle());
    }

}