package dev.geco.gsit.mcv.v26_1.util;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v26_1.entity.PlayerSitEntity;
import dev.geco.gsit.mcv.v26_1.entity.SeatEntity;
import dev.geco.gsit.mcv.v26_1.model.Crawl;
import dev.geco.gsit.mcv.v26_1.model.Pose;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class EntityUtil implements dev.geco.gsit.util.EntityUtil {

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
    public void setEntityLocation(@NotNull Entity entity, @NotNull Location location) {
        if(entity instanceof Player) {
            if(gSitMain.isFoliaServer()) {
                entity.teleportAsync(location, PlayerTeleportEvent.TeleportCause.DISMOUNT);
                return;
            }
            ServerGamePacketListenerImpl serverGamePacketListener = ((CraftPlayer) entity).getHandle().connection;
            serverGamePacketListener.internalTeleport(new PositionMoveRotation(new Vec3(location.getX(), location.getY(), location.getZ()), Vec3.ZERO, location.getYaw(), location.getPitch()), Collections.emptySet());
            serverGamePacketListener.resetPosition();
        } else {
            ((CraftEntity) entity).getHandle().snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }
    }

    @Override
    public boolean isSitLocationValid(@NotNull Location location) { return true; }

    @Override
    public boolean isPlayerSitLocationValid(@NotNull Location location) { return true; }

    @Override
    public @Nullable Entity createSeatEntity(@NotNull Location location, @NotNull Entity entity, boolean canRotate) {
        if(!entity.isValid()) return null;

        net.minecraft.world.entity.Entity rider = ((CraftEntity) entity).getHandle();

        SeatEntity seatEntity = new SeatEntity(location);

        boolean riding = rider.startRiding(seatEntity, true, true);
        if(!riding) return null;
        if(!spawnEntity(seatEntity)) return null;

        if(canRotate) seatEntity.startRotate();

        return seatEntity.getBukkitEntity();
    }

    @Override
    public List<UUID> createPlayerSitEntities(@NotNull Player player, @NotNull Player target) {
        if(!player.isValid()) return null;

        net.minecraft.world.entity.Entity topEntity = ((CraftEntity) target).getHandle();

        int maxEntities = gSitMain.getPlayerSitService().getSitEntityStackCount();
        if(maxEntities <= 0) {
            boolean riding = ((CraftEntity) player).getHandle().startRiding(topEntity, true, true);
            if(riding) return new ArrayList<>();
            return null;
        }

        List<PlayerSitEntity> playerSitEntities = new ArrayList<>();

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {
            PlayerSitEntity playerSitEntity = new PlayerSitEntity(target.getLocation());
            boolean riding = playerSitEntity.startRiding(topEntity, true, true);
            if(!riding) {
                playerSitEntities.forEach(PlayerSitEntity::discard);
                return null;
            }
            if(entityCount == maxEntities) {
                riding = ((CraftEntity) player).getHandle().startRiding(playerSitEntity, true, true);
                if(!riding) {
                    playerSitEntities.forEach(PlayerSitEntity::discard);
                    return null;
                }
            }
            playerSitEntities.add(playerSitEntity);
            if(!spawnEntity(playerSitEntity)) {
                playerSitEntities.forEach(PlayerSitEntity::discard);
                return null;
            }
            topEntity = playerSitEntity;
        }

        return playerSitEntities.stream().map(PlayerSitEntity::getUUID).toList();
    }

    @SuppressWarnings("unchecked")
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
    public @Nullable Pose createPose(@NotNull Seat seat, @NotNull PoseType poseType) { return new Pose(seat, poseType); }

    @Override
    public @Nullable Crawl createCrawl(@NotNull Player player) { return new Crawl(player); }

}