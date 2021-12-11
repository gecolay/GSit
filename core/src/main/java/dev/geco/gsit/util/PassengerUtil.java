package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

public class PassengerUtil {

    private final GSitMain GPM;

    public PassengerUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public long getVehicleAmount(Entity E) {
        long a = 0;
        if(E.isInsideVehicle()) {
            Entity e = E.getVehicle();
            if(!e.hasMetadata(GPM.NAME + "A")) a++;
            a += getVehicleAmount(e);
        }
        return a;
    }

    public long getPassengerAmount(Entity E) {
        long a = 0;
        for(Entity e : E.getPassengers()) {
            if(!e.hasMetadata(GPM.NAME + "A")) a++;
            a += getPassengerAmount(e);
        }
        return a;
    }

    public boolean isInPassengerList(Entity E, Entity S) {
        List<Entity> e = E.getPassengers();
        if(e.contains(S)) return true;
        for(Entity i : e) {
            boolean r = isInPassengerList(i, S);
            if(r) return true;
        }
        return false;
    }

    public Entity getHighestEntity(Entity E) {
        List<Entity> e = E.getPassengers();
        return e.size() == 0 ? E : getHighestEntity(e.get(0));
    }

    public boolean isNPC(Player P) { return !Bukkit.getOnlinePlayers().contains(P); }

}