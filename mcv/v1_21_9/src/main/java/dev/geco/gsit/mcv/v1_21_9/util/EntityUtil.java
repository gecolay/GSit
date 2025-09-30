package dev.geco.gsit.mcv.v1_21_9.util;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_21_9.object.GCrawl;
import dev.geco.gsit.mcv.v1_21_9.object.GPose;
import dev.geco.gsit.mcv.v1_21_9.object.PlayerSeatEntity;
import dev.geco.gsit.mcv.v1_21_9.object.SeatEntity;
import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.IGCrawl;
import dev.geco.gsit.object.IGPose;
import dev.geco.gsit.util.IEntityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class EntityUtil implements IEntityUtil {

    private final GSitMain gSitMain;
    private Field entityManager = null;

    public EntityUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        List<Field> entityManagerFieldList = new ArrayList<>();
        for(Field field : ServerLevel.class.getDeclaredFields()) if(field.getType().equals(PersistentEntitySectionManager.class)) entityManagerFieldList.add(field);
        if(entityManagerFieldList.isEmpty()) return;
        entityManager = entityManagerFieldList.getFirst();
        if(entityManager != null) entityManager.setAccessible(true);
    }

    @Override
    public void setEntityLocation(Entity entity, Location location) { ((CraftEntity) entity).getHandle().absSnapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()); }

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
        if(!gSitMain.getConfigService().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true, true);
        if(!spawnEntity(seatEntity)) return null;
        if(gSitMain.getConfigService().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true, true);
        if(!riding || !seatEntity.passengers.contains(rider)) {
            seatEntity.discard();
            return null;
        }

        if(canRotate) seatEntity.startRotate();

        return seatEntity.getBukkitEntity();
    }

    @Override
    public Set<UUID> createPlayerSeatEntities(Player player, Player target) {
        if(player == null || !player.isValid()) return Collections.emptySet();

        net.minecraft.world.entity.Entity topEntity = ((CraftEntity) target).getHandle();

        int maxEntities = gSitMain.getPlayerSitService().getSeatEntityStackCount();
        if(maxEntities <= 0) {
            ((CraftEntity) player).getHandle().startRiding(topEntity, true, true);
            return Collections.emptySet();
        }

        Set<UUID> playerSeatEntityIds = new HashSet<>();

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {
            net.minecraft.world.entity.Entity playerSeatEntity = new PlayerSeatEntity(target.getLocation());
            playerSeatEntity.startRiding(topEntity, true, true);
            if(entityCount == maxEntities) ((CraftEntity) player).getHandle().startRiding(playerSeatEntity, true, true);
            if(!spawnEntity(playerSeatEntity)) {
                ((CraftEntity) player).getHandle().startRiding(topEntity, true, true);
                return playerSeatEntityIds;
            }
            playerSeatEntityIds.add(playerSeatEntity.getUUID());
            topEntity = playerSeatEntity;
        }

        return playerSeatEntityIds;
    }

    private boolean spawnEntity(net.minecraft.world.entity.Entity entity) {
        if(entityManager != null) {
            try {
                PersistentEntitySectionManager<net.minecraft.world.entity.Entity> entityLookup = (PersistentEntitySectionManager<net.minecraft.world.entity.Entity>) entityManager.get(entity.level().getWorld().getHandle());
                return entityLookup.addNewEntity(entity);
            } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not spawn entity", e); }
            return false;
        }
        try {
            LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter = entity.level().getEntities();
            return (boolean) levelEntityGetter.getClass().getMethod("addNewEntity", net.minecraft.world.entity.Entity.class).invoke(levelEntityGetter, entity);
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not spawn entity", e); }
        return false;
    }

    @Override
    public IGPose createPose(GSeat seat, Pose pose) { return new GPose(seat, pose); }

    @Override
    public IGCrawl createCrawl(Player player) { return new GCrawl(player); }

}