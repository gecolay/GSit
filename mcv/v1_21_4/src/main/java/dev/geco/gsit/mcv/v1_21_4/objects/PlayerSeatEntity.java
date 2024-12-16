package dev.geco.gsit.mcv.v1_21_4.objects;

import java.lang.reflect.*;
import java.util.*;

import com.google.common.collect.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.*;

import net.minecraft.world.entity.*;

public class PlayerSeatEntity extends AreaEffectCloud {

    private final Field vehicle;

    public PlayerSeatEntity(Location Location) {

        super(((CraftWorld) Location.getWorld()).getHandle(), Location.getX(), Location.getY(), Location.getZ());

        persist = false;

        setRadius(0);
        setNoGravity(true);
        setInvulnerable(true);
        addTag("GSit_PlayerSeatEntity");

        List<Field> vehicleList = new ArrayList<>();
        for(Field field : Entity.class.getDeclaredFields()) if(field.getType().equals(Entity.class)) vehicleList.add(field);
        vehicle = vehicleList.getFirst();
        vehicle.setAccessible(true);
    }

    @Override
    public void tick() { }

    @Override
    protected void handlePortal() { }

    @Override
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