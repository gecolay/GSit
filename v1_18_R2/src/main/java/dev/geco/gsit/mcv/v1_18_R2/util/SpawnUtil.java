package dev.geco.gsit.mcv.v1_18_R2.util;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;

import net.minecraft.server.level.ServerLevel;

import dev.geco.gsit.objects.*;
import dev.geco.gsit.mcv.v1_18_R2.objects.*;

public class SpawnUtil implements ISpawnUtil {

    public boolean needCheck() { return false; }

    public boolean check(Location Location) { return true; }

    public Entity createEntity(Location Location) { return createEntity(Location, null); }

    public Entity createEntity(Location Location, Entity Rider) {

        CraftWorld cw = (CraftWorld) Location.getWorld();

        SeatArmorStand sas = new SeatArmorStand(cw.getHandle(), Location.getX(), Location.getY(), Location.getZ());

        if(Rider != null && Rider.isValid()) ((CraftEntity) Rider).getHandle().startRiding(sas, true);

        ServerLevel sl = cw.getHandle();

        sl.entityManager.addNewEntity(sas);

        return sas.getBukkitEntity();
    }

}