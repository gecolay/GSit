package dev.geco.gsit.mcv.v1_19_3.util;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_19_3.objects.GCrawl;
import dev.geco.gsit.mcv.v1_19_3.objects.GPose;
import dev.geco.gsit.mcv.v1_19_3.objects.PlayerSeatEntity;
import dev.geco.gsit.mcv.v1_19_3.objects.SeatEntity;
import dev.geco.gsit.objects.GSeat;
import dev.geco.gsit.objects.IGCrawl;
import dev.geco.gsit.objects.IGPose;
import dev.geco.gsit.util.IEntityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityUtil implements IEntityUtil {

    private final GSitMain gSitMain;
    private Field entityManager = null;

    public EntityUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        if(gSitMain.supportsPaperFeature()) return;
        List<Field> entityManagerFieldList = new ArrayList<>();
        for(Field field : ServerLevel.class.getDeclaredFields()) if(field.getType().equals(PersistentEntitySectionManager.class)) entityManagerFieldList.add(field);
        entityManager = entityManagerFieldList.get(0);
        entityManager.setAccessible(true);
    }

    @Override
    public void setEntityLocation(Entity entity, Location location) { ((CraftEntity) entity).getHandle().moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()); }

    @Override
    public boolean isSitLocationValid(Location location) { return true; }

    @Override
    public boolean isPlayerSitLocationValid(Location location) { return true; }

    @Override
    public Entity createSeatEntity(Location location, Entity entity, boolean canRotate) {
        if(entity == null || !entity.isValid()) return null;

        net.minecraft.world.entity.Entity rider = ((CraftEntity) entity).getHandle();

        SeatEntity seatEntity = new SeatEntity(location);

        boolean riding = true;
        if(!gSitMain.getConfigService().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);
        if(!spawnEntity(seatEntity)) return null;
        if(gSitMain.getConfigService().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);
        if(!riding || !seatEntity.passengers.contains(rider)) {
            seatEntity.discard();
            return null;
        }

        if(canRotate) seatEntity.startRotate();

        return seatEntity.getBukkitEntity();
    }

    @Override
    public UUID createPlayerSeatEntity(Entity holder, Entity entity) {
        if(entity == null || !entity.isValid()) return null;

        net.minecraft.world.entity.Entity lastEntity = ((CraftEntity) holder).getHandle();

        int maxEntities = gSitMain.getPlayerSitService().getSeatEntityStackCount();
        if(maxEntities == 0) {
            ((CraftEntity) entity).getHandle().startRiding(lastEntity, true);
            return null;
        }

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {
            net.minecraft.world.entity.Entity playerSeatEntity = new PlayerSeatEntity(holder.getLocation());
            playerSeatEntity.startRiding(lastEntity, true);

            if(entityCount == maxEntities) ((CraftEntity) entity).getHandle().startRiding(playerSeatEntity, true);

            if(!spawnEntity(playerSeatEntity)) return null;

            lastEntity = playerSeatEntity;
        }

        return lastEntity.getUUID();
    }

    private boolean spawnEntity(net.minecraft.world.entity.Entity Entity) {
        if(!gSitMain.supportsPaperFeature()) {
            try {
                PersistentEntitySectionManager<net.minecraft.world.entity.Entity> entityLookup = (PersistentEntitySectionManager<net.minecraft.world.entity.Entity>) entityManager.get(Entity.level.getWorld().getHandle());
                return entityLookup.addNewEntity(Entity);
            } catch (Throwable e) { e.printStackTrace(); }
            return false;
        }
        LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter = Entity.level.getEntities();
        if(!(levelEntityGetter instanceof io.papermc.paper.chunk.system.entity.EntityLookup entityLookup)) return false;
        return entityLookup.addNewEntity(Entity);
    }

    @Override
    public IGPose createPose(GSeat seat, Pose pose) { return new GPose(seat, pose); }

    @Override
    public IGCrawl createCrawl(Player player) { return new GCrawl(player); }

}