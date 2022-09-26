package dev.geco.gsit.mcv.v1_19_R1_2.util;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_19_R1.*;
import org.bukkit.craftbukkit.v1_19_R1.entity.*;

import net.minecraft.world.level.entity.*;

import dev.geco.gsit.util.*;
import dev.geco.gsit.mcv.v1_19_R1_2.objects.*;

public class SpawnUtil implements ISpawnUtil {

    public boolean needCheck() { return false; }

    public boolean checkLocation(Location Location) { return true; }

    public boolean checkPlayerLocation(Entity Holder) { return true; }

    public Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate) {

        SeatEntity seatEntity = new SeatEntity(Location);

        if(Rider != null && Rider.isValid()) ((CraftEntity) Rider).getHandle().startRiding(seatEntity, true);

        spawnEntity(Location.getWorld(), seatEntity);

        if(Rotate) seatEntity.startRotate();

        return seatEntity.getBukkitEntity();
    }

    public Entity createPlayerSeatEntity(Entity Holder, Entity Rider) {

        PlayerSeatEntity playerSeatEntity = new PlayerSeatEntity(Holder.getLocation());

        if(Rider != null && Rider.isValid()) {

            playerSeatEntity.startRiding(((CraftEntity) Holder).getHandle(), true);

            ((CraftEntity) Rider).getHandle().startRiding(playerSeatEntity, true);
        }

        spawnEntity(Holder.getWorld(), playerSeatEntity);

        return playerSeatEntity.getBukkitEntity();
    }

    private void spawnEntity(World Level, net.minecraft.world.entity.Entity Entity) {

        try {

            ((CraftWorld) Level).getHandle().entityManager.addNewEntity(Entity);
        } catch (Throwable paper) {

            try {

                LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter = ((CraftWorld) Level).getHandle().getEntities();
                levelEntityGetter.getClass().getMethod("addNewEntity", net.minecraft.world.entity.Entity.class).invoke(levelEntityGetter, Entity);
            } catch (Throwable e) { e.printStackTrace(); }
        }
    }

}