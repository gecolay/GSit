package dev.geco.gsit.mcv.v1_19_R2.util;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R2.*;
import org.bukkit.craftbukkit.v1_19_R2.entity.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;

import net.minecraft.world.level.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.util.*;
import dev.geco.gsit.mcv.v1_19_R2.objects.*;

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

    public void createPlayerSeatEntity(Entity Holder, Entity Rider) {

        if(Rider == null || !Rider.isValid()) return;

        Entity lastEntity = Holder;

        int maxEntities = GSitMain.getInstance().PLAYER_SIT_SEAT_ENTITIES;

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {

            PlayerSeatEntity playerSeatEntity = new PlayerSeatEntity(lastEntity.getLocation());

            playerSeatEntity.getBukkitEntity().setMetadata(GSitMain.getInstance().NAME + "A", new FixedMetadataValue(GSitMain.getInstance(), lastEntity));

            playerSeatEntity.startRiding(((CraftEntity) lastEntity).getHandle(), true);

            if(entityCount == maxEntities) ((CraftEntity) Rider).getHandle().startRiding(playerSeatEntity, true);

            spawnEntity(lastEntity.getWorld(), playerSeatEntity);

            lastEntity = playerSeatEntity.getBukkitEntity();
        }
    }

    private boolean spawnEntity(World Level, net.minecraft.world.entity.Entity Entity) {

        try {

            return ((CraftWorld) Level).getHandle().entityManager.addNewEntity(Entity);
        } catch (Throwable paper) {

            try {

                LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter = ((CraftWorld) Level).getHandle().getEntities();
                return (boolean) levelEntityGetter.getClass().getMethod("addNewEntity", net.minecraft.world.entity.Entity.class).invoke(levelEntityGetter, Entity);
            } catch (Throwable e) { e.printStackTrace(); }
        }

        return false;
    }

}