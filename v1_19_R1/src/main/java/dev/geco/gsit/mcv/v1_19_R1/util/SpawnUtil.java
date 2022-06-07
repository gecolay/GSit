package dev.geco.gsit.mcv.v1_19_R1.util;

import dev.geco.gsit.mcv.v1_19_R1.objects.SeatEntity;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;

import dev.geco.gsit.objects.*;
import dev.geco.gsit.mcv.v1_19_R1.objects.*;

public class SpawnUtil implements ISpawnUtil {

    public boolean needCheck() { return false; }

    public boolean checkLocation(Location Location) { return true; }

    public Entity createSeatEntity(Location Location) { return createSeatEntity(Location, null); }

    public Entity createSeatEntity(Location Location, Entity Rider) {

        SeatEntity sas = new SeatEntity(Location);

        if(Rider != null && Rider.isValid()) ((CraftEntity) Rider).getHandle().startRiding(sas, true);

        ((CraftWorld) Location.getWorld()).getHandle().entityManager.addNewEntity(sas);

        return sas.getBukkitEntity();
    }

}