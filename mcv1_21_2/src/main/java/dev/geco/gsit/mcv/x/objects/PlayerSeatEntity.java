package dev.geco.gsit.mcv.x.objects;

import java.lang.reflect.*;
import java.util.*;

import com.google.common.collect.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_21_R2.*;

import net.minecraft.world.entity.*;

public class PlayerSeatEntity extends AreaEffectCloud {

    private final Field vehicle;

    public PlayerSeatEntity(Location Location) {

        super(((CraftWorld) Location.getWorld()).getHandle(), Location.getX(), Location.getY(), Location.getZ());

        persist = false;

        setRadius(0);
        setNoGravity(true);
        setInvulnerable(true);
        setDuration(Integer.MAX_VALUE);
        setWaitTime(0);
        addTag("GSit_PlayerSeatEntity");

        List<Field> vehicleList = new ArrayList<>();
        for(Field field : Entity.class.getDeclaredFields()) if(field.getType().equals(Entity.class)) vehicleList.add(field);
        vehicle = vehicleList.getFirst();
        vehicle.setAccessible(true);
    }

    public void tick() { }

    protected void handlePortal() { }

    public boolean dismountsUnderwater() { return false; }

    public void setVehicle(Entity Vehicle) {

        try {
            vehicle.set(this, Vehicle);
        } catch (Throwable ignored) { }

        if(Vehicle.passengers.isEmpty()) {
            Vehicle.passengers = ImmutableList.of(this);
        } else {
            List<Entity> list = Lists.newArrayList(Vehicle.passengers);
            list.add(this);
            Vehicle.passengers = ImmutableList.copyOf(list);
        }

        Vehicle.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ENTITY_MOUNT, this);
    }

}