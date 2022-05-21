package dev.geco.gsit.mcv.v1_17_R1_2.util;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;

import dev.geco.gsit.objects.*;
import dev.geco.gsit.mcv.v1_17_R1_2.objects.*;

public class SpawnUtil implements ISpawnUtil {

    public boolean needCheck() { return false; }

    public boolean checkLocation(Location Location) { return true; }

    public Entity createSeatEntity(Location Location) { return createSeatEntity(Location, null); }

    public Entity createSeatEntity(Location Location, Entity Rider) {

        CraftWorld cw = (CraftWorld) Location.getWorld();

        SeatEntity sas = new SeatEntity(cw.getHandle(), Location.getX(), Location.getY(), Location.getZ());

        if(Rider != null && Rider.isValid()) ((CraftEntity) Rider).getHandle().startRiding(sas, true);

        cw.getHandle().entityManager.addNewEntity(sas);

        return sas.getBukkitEntity();
    }

}