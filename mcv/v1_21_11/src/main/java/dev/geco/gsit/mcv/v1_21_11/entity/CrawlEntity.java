package dev.geco.gsit.mcv.v1_21_11.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CrawlEntity extends HappyGhast {

    public CrawlEntity(Location Location) {
        super(EntityType.HAPPY_GHAST, ((CraftWorld) Location.getWorld()).getHandle());
        setPos(Location.getX(), Location.getY(), Location.getZ());
        persist = false;
        setInvisible(true);
        setNoGravity(true);
        setInvulnerable(true);
        setNoAi(true);
        setSilent(true);

        List<Field> entityDataAccessorList = new ArrayList<>();
        for(Field field : HappyGhast.class.getDeclaredFields()) if(field.getType().equals(EntityDataAccessor.class)) entityDataAccessorList.add(field);
        Field staysStill = entityDataAccessorList.getLast();
        staysStill.setAccessible(true);
        try { this.entityData.set((EntityDataAccessor<Boolean>) staysStill.get(this), true); } catch(Throwable ignored) { }
    }

    @Override
    protected void handlePortal() { }

    @Override
    public boolean isAffectedByFluids() { return false; }

}