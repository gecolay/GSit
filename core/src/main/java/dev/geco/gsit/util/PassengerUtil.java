package dev.geco.gsit.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class PassengerUtil {

    public long getEntityVehicleCount(Entity entity) {
        long vehicleCount = 0;
        Entity current = entity.getVehicle();
        while (current != null) {
            if (current instanceof Player) vehicleCount++;
            current = current.getVehicle();
        }
        return vehicleCount;
    }

    public long getEntityPassengerCount(Entity entity) {
        long passengerCount = 0;
        if (entity.getPassengers().isEmpty()) return passengerCount;
        
        Queue<Entity> queue = new ArrayDeque<>(entity.getPassengers());
        while (!queue.isEmpty()) {
            Entity passenger = queue.poll();
            if (passenger instanceof Player) passengerCount++;
            queue.addAll(passenger.getPassengers());
        }
        return passengerCount;
    }

    public boolean isEntityInEntityPassengerList(Entity entity, Entity target) {
        if (entity == null || target == null) return false;
        
        Deque<Entity> stack = new ArrayDeque<>(entity.getPassengers());
        while (!stack.isEmpty()) {
            Entity passenger = stack.pop();
            if (passenger.equals(target)) return true;
            stack.addAll(passenger.getPassengers());
        }
        return false;
    }

    public Entity getTopEntityPassenger(Entity entity) {
        if (entity == null) return null;
        
        Entity top = entity;
        while (!top.getPassengers().isEmpty()) {
            top = top.getPassengers().get(0);
        }
        return top;
    }

    public Entity getBottomEntityVehicle(Entity entity) {
        if (entity == null) return null;
        
        Entity bottom = entity;
        while (bottom.getVehicle() != null) {
            bottom = bottom.getVehicle();
        }
        return bottom;
    }
}
