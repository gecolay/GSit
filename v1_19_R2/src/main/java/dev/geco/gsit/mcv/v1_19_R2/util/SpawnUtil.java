package dev.geco.gsit.mcv.v1_19_R2.util;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R2.*;
import org.bukkit.craftbukkit.v1_19_R2.entity.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.util.*;
import dev.geco.gsit.mcv.v1_19_R2.objects.*;

public class SpawnUtil implements ISpawnUtil {

    private final GSitMain GPM = GSitMain.getInstance();

    public boolean needCheck() { return false; }

    public boolean checkLocation(Location Location) { return true; }

    public boolean checkPlayerLocation(Entity Holder) { return true; }

    public Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate) {

        if(Rider == null || !Rider.isValid()) return null;

        boolean riding = true;

        net.minecraft.world.entity.Entity rider = ((CraftEntity) Rider).getHandle();

        SeatEntity seatEntity = new SeatEntity(Location);

        if(!GPM.getCManager().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);

        spawnEntity(Location.getWorld(), seatEntity);

        if(GPM.getCManager().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);

        if(!riding || !seatEntity.passengers.contains(rider)) {

            seatEntity.discard();
            return null;
        }

        if(Rotate) seatEntity.startRotate();

        return seatEntity.getBukkitEntity();
    }

    public void createPlayerSeatEntity(Entity Holder, Entity Rider) {

        if(Rider == null || !Rider.isValid()) return;

        Entity lastEntity = Holder;

        int maxEntities = GPM.PLAYER_SIT_SEAT_ENTITIES;

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {

            PlayerSeatEntity playerSeatEntity = new PlayerSeatEntity(lastEntity.getLocation());

            playerSeatEntity.getBukkitEntity().setMetadata(GPM.NAME + "A", new FixedMetadataValue(GPM, lastEntity));

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

                net.minecraft.world.level.entity.LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter = ((CraftWorld) Level).getHandle().getEntities();
                return (boolean) levelEntityGetter.getClass().getMethod("addNewEntity", net.minecraft.world.entity.Entity.class).invoke(levelEntityGetter, Entity);
            } catch (Throwable e) { e.printStackTrace(); }
        }

        return false;
    }

}