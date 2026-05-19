package dev.geco.gsit.mcv.v1_18.util;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_18.entity.PlayerSitEntity;
import dev.geco.gsit.mcv.v1_18.entity.SeatEntity;
import dev.geco.gsit.mcv.v1_18.model.Crawl;
import dev.geco.gsit.mcv.v1_18.model.Pose;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityUtil implements dev.geco.gsit.util.EntityUtil {

    private final GSitMain gSitMain;

    public EntityUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public void setEntityLocation(@NotNull Entity entity, @NotNull Location location) {
        if(entity instanceof Player) {
            ServerGamePacketListenerImpl serverGamePacketListener = ((CraftPlayer) entity).getHandle().connection;
            serverGamePacketListener.teleport(location);
            serverGamePacketListener.resetPosition();
        } else {
            ((CraftEntity) entity).getHandle().moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
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

        boolean riding = rider.startRiding(seatEntity, true);
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
            boolean riding = ((CraftEntity) player).getHandle().startRiding(topEntity, true);
            if(riding) return new ArrayList<>();
            return null;
        }

        List<PlayerSitEntity> playerSitEntities = new ArrayList<>();

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {
            PlayerSitEntity playerSitEntity = new PlayerSitEntity(target.getLocation());
            boolean riding = playerSitEntity.startRiding(topEntity, true);
            if(!riding) {
                playerSitEntities.forEach(PlayerSitEntity::discard);
                return null;
            }
            if(entityCount == maxEntities) {
                riding = ((CraftEntity) player).getHandle().startRiding(playerSitEntity, true);
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

    private boolean spawnEntity(net.minecraft.world.entity.Entity entity) { return entity.level.getWorld().getHandle().entityManager.addNewEntity(entity); }

    @Override
    public @Nullable Pose createPose(@NotNull Seat seat, @NotNull PoseType poseType) { return new Pose(seat, poseType); }

    @Override
    public @Nullable Crawl createCrawl(@NotNull Player player) { return new Crawl(player); }

}