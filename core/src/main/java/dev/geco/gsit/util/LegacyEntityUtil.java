package dev.geco.gsit.util;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.Pose;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.service.PlayerSitService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class LegacyEntityUtil implements EntityUtil {

    private final GSitMain gSitMain;

    public LegacyEntityUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public void setEntityLocation(@NotNull Entity entity, @NotNull Location location) {
        try {
            Method getHandle = entity.getClass().getMethod("getHandle");
            Object serverEntity = getHandle.invoke(entity);
            Method setPositionRotationMethod = serverEntity.getClass().getMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
            setPositionRotationMethod.invoke(serverEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not set entity location", e); }
    }

    @Override
    @SuppressWarnings("removal")
    public boolean isSitLocationValid(@NotNull Location location) {
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
    @SuppressWarnings("removal")
    public boolean isPlayerSitLocationValid(@NotNull Location location) {
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
    @SuppressWarnings("removal")
    public @Nullable Entity createSeatEntity(@NotNull Location location, @NotNull Entity entity, boolean canRotate) {
        if(!entity.isValid()) return null;

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
                riding[0] = armorStand.addPassenger(entity);
            };

            World world = location.getWorld();
            Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
            Entity seatEntity = (Entity) spawnMethod.invoke(world, location, ArmorStand.class, consumer);
            if(!riding[0]) return null;

            return seatEntity;
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not spawn entity", e); }
        return null;
    }

    @Override
    @SuppressWarnings("removal")
    public @Nullable List<UUID> createPlayerSitEntities(@NotNull Player player, @NotNull Player target) {
        if(!player.isValid()) return null;

        Entity topEntity = target;

        int maxEntities = gSitMain.getPlayerSitService().getSitEntityStackCount();
        if(maxEntities <= 0) {
            boolean riding = topEntity.addPassenger(player);
            if(riding) return new ArrayList<>();
            return null;
        }

        List<AreaEffectCloud> playerSitEntities = new ArrayList<>();

        try {
            World world = target.getWorld();
            Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);

            for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {
                final boolean[] riding = { true };
                Entity finalTopEntity = topEntity;
                int finalEntityCount = entityCount;
                org.bukkit.util.Consumer<AreaEffectCloud> areaEffectCloudConsumer = (areaEffectCloud) -> {
                    try { areaEffectCloud.setRadius(0); } catch(Throwable ignored) { }
                    try { areaEffectCloud.setDuration(Integer.MAX_VALUE); } catch(Throwable ignored) { }
                    try { areaEffectCloud.setGravity(false); } catch(Throwable ignored) { }
                    try { areaEffectCloud.setInvulnerable(true); } catch(Throwable ignored) { }
                    areaEffectCloud.addScoreboardTag(PlayerSitService.PLAYERSIT_ENTITY_TAG);
                    riding[0] = finalTopEntity.addPassenger(areaEffectCloud);
                    if(!riding[0]) return;
                    if(finalEntityCount == maxEntities) riding[0] = areaEffectCloud.addPassenger(player);
                };
                AreaEffectCloud playerSitEntity = (AreaEffectCloud) spawnMethod.invoke(world, target.getLocation(), AreaEffectCloud.class, areaEffectCloudConsumer);
                playerSitEntities.add(playerSitEntity);
                topEntity = playerSitEntity;

            }
        } catch(Throwable e) { gSitMain.getLogger().log(Level.SEVERE, "Could not spawn entity", e); }
        return playerSitEntities.stream().map(AreaEffectCloud::getUniqueId).toList();
    }

    @Override
    public @Nullable Pose createPose(@NotNull Seat seat, @NotNull PoseType poseType) { return null; }

    @Override
    public @Nullable Crawl createCrawl(@NotNull Player player) { return null; }

}