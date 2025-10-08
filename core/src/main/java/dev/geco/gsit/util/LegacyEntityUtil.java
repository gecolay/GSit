package dev.geco.gsit.util;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.Pose;
import dev.geco.gsit.service.PlayerSitService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class LegacyEntityUtil implements EntityUtil {

    private final GSitMain gSitMain;

    public LegacyEntityUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public void setEntityLocation(Entity entity, Location location) {
        try {
            Method getHandle = entity.getClass().getMethod("getHandle");
            Object serverEntity = getHandle.invoke(entity);
            Method setPositionRotationMethod = serverEntity.getClass().getMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
            setPositionRotationMethod.invoke(serverEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not set entity location", e); }
    }

    @Override
    public boolean isSitLocationValid(Location location) {
        try {
            org.bukkit.util.Consumer<ArmorStand> armorStandConsumer = (armorStand) -> {
                try { armorStand.setInvisible(true); } catch(Throwable e) { try { ArmorStand.class.getMethod("setVisible", boolean.class).invoke(armorStand, false); } catch(Throwable ignored) { } }
                try { armorStand.setMarker(true); } catch(Throwable ignored) { }
            };

            World world = location.getWorld();
            Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
            Entity seatEntity = (Entity) spawnMethod.invoke(world, location, ArmorStand.class, armorStandConsumer);
            boolean valid = seatEntity.isValid();
            seatEntity.remove();

            return valid;
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not check sit location", e); }
        return false;
    }

    @Override
    public boolean isPlayerSitLocationValid(Location location) {
        try {
            org.bukkit.util.Consumer<AreaEffectCloud> areaEffectCloudConsumer = (areaEffectCloud) -> {
                try { areaEffectCloud.setRadius(0); } catch(Throwable ignored) { }
            };

            World world = location.getWorld();
            Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
            Entity playerSitEntity = (Entity) spawnMethod.invoke(world, location, AreaEffectCloud.class, areaEffectCloudConsumer);
            boolean valid = playerSitEntity.isValid();
            playerSitEntity.remove();

            return valid;
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not check player sit location", e); }
        return false;
    }

    @Override
    public Entity createSeatEntity(Location location, Entity entity, boolean canRotate) {
        try {
            final boolean[] riding = { true };
            org.bukkit.util.Consumer<ArmorStand> consumer = (armorStand) -> {

                try { armorStand.setInvisible(true); } catch(Throwable e) { try { ArmorStand.class.getMethod("setVisible", boolean.class).invoke(armorStand, false); } catch(Throwable ignored) { } }
                try { armorStand.setGravity(false); } catch(Throwable ignored) { }
                try { armorStand.setMarker(true); } catch(Throwable ignored) { }
                try { armorStand.setInvulnerable(true); } catch(Throwable ignored) { }
                try { armorStand.setSmall(true); } catch(Throwable ignored) { }
                try { armorStand.setBasePlate(false); } catch(Throwable ignored) { }
                armorStand.addScoreboardTag(GSitMain.NAME + "_SeatEntity");
                if(!gSitMain.getConfigService().ENHANCED_COMPATIBILITY && entity != null && entity.isValid()) riding[0] = armorStand.addPassenger(entity);
            };

            World world = location.getWorld();
            Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
            Entity seatEntity = (Entity) spawnMethod.invoke(world, location, ArmorStand.class, consumer);
            if(gSitMain.getConfigService().ENHANCED_COMPATIBILITY && entity != null && entity.isValid()) riding[0] = seatEntity.addPassenger(entity);
            if(entity != null && entity.isValid() && (!riding[0] || !seatEntity.getPassengers().contains(entity))) {
                seatEntity.remove();
                return null;
            }

            return seatEntity;
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not spawn entity", e); }
        return null;
    }

    @Override
    public Set<UUID> createPlayerSitEntities(Player player, Player target) {
        if(player == null || !player.isValid()) return Collections.emptySet();

        int maxEntities = gSitMain.getPlayerSitService().getSitEntityStackCount();
        Entity lastEntity = target;
        Set<UUID> playerSitEntityIds = new HashSet<>();
        try {
            World world = target.getWorld();
            Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);

            for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {
                Entity finalLastEntity = lastEntity;
                int finalEntityCount = entityCount;

                org.bukkit.util.Consumer<AreaEffectCloud> areaEffectCloudConsumer = (areaEffectCloud) -> {

                    try { areaEffectCloud.setRadius(0); } catch(Throwable ignored) { }
                    try { areaEffectCloud.setDuration(Integer.MAX_VALUE); } catch(Throwable ignored) { }
                    try { areaEffectCloud.setGravity(false); } catch(Throwable ignored) { }
                    try { areaEffectCloud.setInvulnerable(true); } catch(Throwable ignored) { }
                    areaEffectCloud.addScoreboardTag(PlayerSitService.PLAYERSIT_ENTITY_TAG);
                    finalLastEntity.addPassenger(areaEffectCloud);
                    if(finalEntityCount == maxEntities) areaEffectCloud.addPassenger(player);
                };

                lastEntity = (Entity) spawnMethod.invoke(world, finalLastEntity.getLocation(), AreaEffectCloud.class, areaEffectCloudConsumer);
                playerSitEntityIds.add(lastEntity.getUniqueId());
            }
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not spawn entity", e); }
        return playerSitEntityIds;
    }

    @Override
    public Pose createPose(Seat seat, PoseType poseType) { return null; }

    @Override
    public Crawl createCrawl(Player player) { return null; }

}