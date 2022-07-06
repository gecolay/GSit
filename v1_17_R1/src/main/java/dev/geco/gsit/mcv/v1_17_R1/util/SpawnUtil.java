package dev.geco.gsit.mcv.v1_17_R1.util;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_17_R1.*;
import org.bukkit.craftbukkit.v1_17_R1.entity.*;

import dev.geco.gsit.util.*;
import dev.geco.gsit.mcv.v1_17_R1.objects.*;

public class SpawnUtil implements ISpawnUtil {

    private final dev.geco.gsit.util.SpawnUtil spawnUtil = new dev.geco.gsit.util.SpawnUtil();

    public boolean needCheck() { return false; }

    public boolean checkLocation(Location Location) { return true; }

    public boolean checkPlayerLocation(Entity Holder) { return spawnUtil.checkPlayerLocation(Holder); }

    public Entity createSeatEntity(Location Location, Entity Rider) {

        SeatEntity seatEntity = new SeatEntity(Location);

        if(Rider != null && Rider.isValid()) ((CraftEntity) Rider).getHandle().startRiding(seatEntity, true);

        ((CraftWorld) Location.getWorld()).getHandle().entityManager.addNewEntity(seatEntity);

        return seatEntity.getBukkitEntity();
    }

    public Entity createPlayerSeatEntity(Entity Holder, Entity Rider) { return spawnUtil.createPlayerSeatEntity(Holder, Rider); }

}