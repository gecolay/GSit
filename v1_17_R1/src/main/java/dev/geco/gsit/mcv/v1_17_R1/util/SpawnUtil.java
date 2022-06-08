package dev.geco.gsit.mcv.v1_17_R1.util;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;

import dev.geco.gsit.util.*;
import dev.geco.gsit.mcv.v1_17_R1.objects.*;

public class SpawnUtil implements ISpawnUtil {

    private final dev.geco.gsit.util.SpawnUtil spawnutil = new dev.geco.gsit.util.SpawnUtil();

    public boolean needCheck() { return false; }

    public boolean checkLocation(Location Location) { return true; }

    public boolean checkPlayerLocation(Entity Holder) { return spawnutil.checkPlayerLocation(Holder); }

    public Entity createSeatEntity(Location Location, Entity Rider) {

        SeatEntity sas = new SeatEntity(Location);

        if(Rider != null && Rider.isValid()) ((CraftEntity) Rider).getHandle().startRiding(sas, true);

        ((CraftWorld) Location.getWorld()).getHandle().entityManager.addNewEntity(sas);

        return sas.getBukkitEntity();
    }

    public Entity createPlayerSeatEntity(Entity Holder, Entity Rider) { return spawnutil.createPlayerSeatEntity(Holder, Rider); }

}