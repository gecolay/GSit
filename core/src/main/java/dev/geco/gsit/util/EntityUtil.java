package dev.geco.gsit.util;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.GSeat;
import dev.geco.gsit.objects.IGCrawl;
import dev.geco.gsit.objects.IGPose;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.lang.reflect.Method;
import java.util.UUID;

public class EntityUtil implements IEntityUtil {

    private final GSitMain gSitMain;

    public EntityUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
    }

    @Override
    public void setEntityLocation(Entity entity, Location location) {
        try {
            Method getHandle = entity.getClass().getMethod("getHandle");
            Object serverEntity = getHandle.invoke(entity);
            Method setPositionRotationMethod = serverEntity.getClass().getMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
            setPositionRotationMethod.invoke(serverEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } catch (Throwable e) { e.printStackTrace(); }
    }

    @Override
    public boolean isSitLocationValid(Location location) {
        try {
            org.bukkit.util.Consumer<ArmorStand> armorStandConsumer = (armorStand) -> {
                try { armorStand.setInvisible(true); } catch (Throwable e) { try { ArmorStand.class.getMethod("setVisible", boolean.class).invoke(armorStand, false); } catch (Throwable ignored) { } }
                try { armorStand.setMarker(true); } catch (Throwable ignored) { }
            };

            World world = location.getWorld();
            Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
            Entity seatEntity = (Entity) spawnMethod.invoke(world, location, ArmorStand.class, armorStandConsumer);
            boolean valid = seatEntity.isValid();
            seatEntity.remove();

            return valid;
        } catch (Throwable e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean isPlayerSitLocationValid(Location location) {
        try {
            org.bukkit.util.Consumer<AreaEffectCloud> areaEffectCloudConsumer = (areaEffectCloud) -> {
                try { areaEffectCloud.setRadius(0); } catch (Throwable ignored) { }
            };

            World world = location.getWorld();
            Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
            Entity playerSeatEntity = (Entity) spawnMethod.invoke(world, location, AreaEffectCloud.class, areaEffectCloudConsumer);
            boolean valid = playerSeatEntity.isValid();
            playerSeatEntity.remove();

            return valid;
        } catch (Throwable e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public Entity createSeatEntity(Location location, Entity entity, boolean canRotate) {
        try {
            final boolean[] riding = { true };
            org.bukkit.util.Consumer<ArmorStand> consumer = (armorStand) -> {

                try { armorStand.setInvisible(true); } catch (Throwable e) { try { ArmorStand.class.getMethod("setVisible", boolean.class).invoke(armorStand, false); } catch (Throwable ignored) { } }
                try { armorStand.setGravity(false); } catch (Throwable ignored) { }
                try { armorStand.setMarker(true); } catch (Throwable ignored) { }
                try { armorStand.setInvulnerable(true); } catch (Throwable ignored) { }
                try { armorStand.setSmall(true); } catch (Throwable ignored) { }
                try { armorStand.setBasePlate(false); } catch (Throwable ignored) { }
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
        } catch (Throwable e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public UUID createPlayerSeatEntity(Entity holder, Entity entity) {
        if(entity == null || !entity.isValid()) return null;

        int maxEntities = gSitMain.getPlayerSitService().getSeatEntityStackCount();
        if(maxEntities == 0) {
            holder.addPassenger(entity);
            return null;
        }

        Entity lastEntity = holder;
        try {
            World world = holder.getWorld();
            Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);

            for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {
                Entity finalLastEntity = lastEntity;
                int finalEntityCount = entityCount;

                org.bukkit.util.Consumer<AreaEffectCloud> areaEffectCloudConsumer = (areaEffectCloud) -> {

                    try { areaEffectCloud.setRadius(0); } catch (Throwable ignored) { }
                    try { areaEffectCloud.setGravity(false); } catch (Throwable ignored) { }
                    try { areaEffectCloud.setInvulnerable(true); } catch (Throwable ignored) { }
                    areaEffectCloud.addScoreboardTag(GSitMain.NAME + "_PlayerSeatEntity");
                    finalLastEntity.addPassenger(areaEffectCloud);
                    if(finalEntityCount == maxEntities) areaEffectCloud.addPassenger(entity);
                };

                lastEntity = (Entity) spawnMethod.invoke(world, finalLastEntity.getLocation(), AreaEffectCloud.class, areaEffectCloudConsumer);
            }
        } catch (Throwable e) { e.printStackTrace(); }
        return lastEntity.getUniqueId();
    }

    @Override
    public IGPose createPose(GSeat seat, Pose pose) { return null; }

    @Override
    public IGCrawl createCrawl(Player player) { return null; }

}