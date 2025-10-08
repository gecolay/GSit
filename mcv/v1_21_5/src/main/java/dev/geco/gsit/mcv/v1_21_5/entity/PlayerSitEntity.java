package dev.geco.gsit.mcv.v1_21_5.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dev.geco.gsit.service.PlayerSitService;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PlayerSitEntity extends AreaEffectCloud {

    private final Field vehicle;

    public PlayerSitEntity(Location location) {
        super(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ());
        persist = false;
        setRadius(0);
        setDuration(Integer.MAX_VALUE);
        setNoGravity(true);
        setInvulnerable(true);
        addTag(PlayerSitService.PLAYERSIT_ENTITY_TAG);
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

    public void setVehicle(Entity vehicle) {
        try { this.vehicle.set(this, vehicle); } catch(Throwable ignored) { }
        if(vehicle.passengers.isEmpty()) vehicle.passengers = ImmutableList.of(this);
        else {
            List<Entity> list = Lists.newArrayList(vehicle.passengers);
            list.add(this);
            vehicle.passengers = ImmutableList.copyOf(list);
        }
        vehicle.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ENTITY_MOUNT, this);
    }

}