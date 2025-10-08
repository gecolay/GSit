package dev.geco.gsit.mcv.v1_19.util;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_19.model.Crawl;
import dev.geco.gsit.mcv.v1_19.model.Pose;
import dev.geco.gsit.mcv.v1_19.entity.PlayerSitEntity;
import dev.geco.gsit.mcv.v1_19.entity.SeatEntity;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntityUtil implements dev.geco.gsit.util.EntityUtil {

    private final GSitMain gSitMain;

    public EntityUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
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
    public Set<UUID> createPlayerSitEntities(Player player, Player target) {
        if(player == null || !player.isValid()) return Collections.emptySet();

        net.minecraft.world.entity.Entity topEntity = ((CraftEntity) target).getHandle();

        int maxEntities = gSitMain.getPlayerSitService().getSitEntityStackCount();
        if(maxEntities <= 0) {
            ((CraftEntity) player).getHandle().startRiding(topEntity, true);
            return Collections.emptySet();
        }

        Set<UUID> playerSitEntityIds = new HashSet<>();

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {
            net.minecraft.world.entity.Entity playerSitEntity = new PlayerSitEntity(target.getLocation());
            playerSitEntity.startRiding(topEntity, true);
            if(entityCount == maxEntities) ((CraftEntity) player).getHandle().startRiding(playerSitEntity, true);
            if(!spawnEntity(playerSitEntity)) {
                ((CraftEntity) player).getHandle().startRiding(topEntity, true);
                return playerSitEntityIds;
            }
            playerSitEntityIds.add(playerSitEntity.getUUID());
            topEntity = playerSitEntity;
        }

        return playerSitEntityIds;
    }

    private boolean spawnEntity(net.minecraft.world.entity.Entity entity) { return entity.level.getWorld().getHandle().entityManager.addNewEntity(entity); }

    @Override
    public dev.geco.gsit.model.Pose createPose(Seat seat, PoseType poseType) { return new Pose(seat, poseType); }

    @Override
    public dev.geco.gsit.model.Crawl createCrawl(Player player) { return new Crawl(player); }

}