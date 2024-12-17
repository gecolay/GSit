package dev.geco.gsit.mcv.v1_20.util;

import java.lang.reflect.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.entity.*;
import org.bukkit.entity.*;

import net.minecraft.server.level.*;
import net.minecraft.world.level.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_20.objects.*;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.util.*;

public class EntityUtil implements IEntityUtil {

    private final GSitMain GPM;
    private Field entityManager = null;

    public EntityUtil(GSitMain GPluginMain) {
        GPM = GPluginMain;

        if(GPM.supportsPaperFeature()) return;
        List<Field> entityManagerFieldList = new ArrayList<>();
        for(Field field : ServerLevel.class.getDeclaredFields()) if(field.getType().equals(PersistentEntitySectionManager.class)) entityManagerFieldList.add(field);
        entityManager = entityManagerFieldList.get(0);
        entityManager.setAccessible(true);
    }

    @Override
    public void setEntityLocation(Entity Entity, Location Location) { ((CraftEntity) Entity).getHandle().moveTo(Location.getX(), Location.getY(), Location.getZ(), Location.getYaw(), Location.getPitch()); }

    @Override
    public boolean isLocationValid(Location Location) { return true; }

    @Override
    public boolean isPlayerSitLocationValid(Entity Holder) { return true; }

    @Override
    public Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate) {

        if(Rider == null || !Rider.isValid()) return null;

        boolean riding = true;

        net.minecraft.world.entity.Entity rider = ((CraftEntity) Rider).getHandle();

        SeatEntity seatEntity = new SeatEntity(Location);

        if(!GPM.getCManager().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);

        if(!spawnEntity(seatEntity)) return null;

        if(GPM.getCManager().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);

        if(!riding || !seatEntity.passengers.contains(rider)) {

            seatEntity.discard();
            return null;
        }

        if(Rotate) seatEntity.startRotate();

        return seatEntity.getBukkitEntity();
    }

    @Override
    public UUID createPlayerSeatEntity(Entity Holder, Entity Rider) {

        if(Rider == null || !Rider.isValid()) return null;

        net.minecraft.world.entity.Entity lastEntity = ((CraftEntity) Holder).getHandle();

        int maxEntities = GPM.getPlayerSitManager().getSeatEntityCount();

        if(maxEntities == 0) {

            ((CraftEntity) Rider).getHandle().startRiding(lastEntity, true);
            return null;
        }

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {

            net.minecraft.world.entity.Entity playerSeatEntity = new PlayerSeatEntity(Holder.getLocation());

            playerSeatEntity.startRiding(lastEntity, true);

            if(entityCount == maxEntities) ((CraftEntity) Rider).getHandle().startRiding(playerSeatEntity, true);

            if(!spawnEntity(playerSeatEntity)) return null;

            lastEntity = playerSeatEntity;
        }

        return lastEntity.getUUID();
    }

    private boolean spawnEntity(net.minecraft.world.entity.Entity Entity) {

        if(!GPM.supportsPaperFeature()) {
            try {
                PersistentEntitySectionManager<net.minecraft.world.entity.Entity> entityLookup = (PersistentEntitySectionManager<net.minecraft.world.entity.Entity>) entityManager.get(Entity.level().getWorld().getHandle());
                return entityLookup.addNewEntity(Entity);
            } catch (Throwable e) { e.printStackTrace(); }
            return false;
        }

        LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter = Entity.level().getEntities();
        if(!(levelEntityGetter instanceof io.papermc.paper.chunk.system.entity.EntityLookup entityLookup)) return false;
        return entityLookup.addNewEntity(Entity);
    }

    @Override
    public IGPoseSeat createPoseSeatObject(GSeat Seat, Pose Pose) { return new GPoseSeat(Seat, Pose); }

    @Override
    public IGCrawl createCrawlObject(Player Player) { return new GCrawl(Player); }

}