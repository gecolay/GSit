package dev.geco.gsit.util;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.IGCrawl;
import dev.geco.gsit.object.IGPose;
import dev.geco.gsit.service.PlayerSitService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.util.Consumer;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class EntityUtil implements IEntityUtil {

    private final GSitMain gSitMain;
    private final Method spawnMethod;
    private final boolean enhancedCompatibility;
    private final int seatEntityStackCount;

    public EntityUtil(GSitMain gSitMain) {
        this.gSitMain = gSitMain;
        this.enhancedCompatibility = gSitMain.getConfigService().ENHANCED_COMPATIBILITY;
        this.seatEntityStackCount = gSitMain.getPlayerSitService().getSeatEntityStackCount();
        
        Method spawn = null;
        try {
            spawn = World.class.getMethod("spawn", Location.class, Class.class, Consumer.class);
        } catch (NoSuchMethodException e) {
            gSitMain.getLogger().log(Level.SEVERE, "Could not find spawn method", e);
        }
        this.spawnMethod = spawn;
    }

    @Override
    public void setEntityLocation(Entity entity, Location location) {
        try {
            Object serverEntity = entity.getClass().getMethod("getHandle").invoke(entity);
            serverEntity.getClass().getMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class)
                .invoke(serverEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } catch(Throwable e) { 
            gSitMain.getLogger().log(Level.SEVERE, "Could not set entity location", e); 
        }
    }

    @Override
    public boolean isSitLocationValid(Location location) {
        return validateLocation(location, ArmorStand.class, armorStand -> {
            setArmorStandFlags(armorStand);
        });
    }

    @Override
    public boolean isPlayerSitLocationValid(Location location) {
        return validateLocation(location, AreaEffectCloud.class, cloud -> {
            try { cloud.setRadius(0); } catch(Throwable ignored) { }
        });
    }

    private <T extends Entity> boolean validateLocation(Location location, Class<T> entityClass, Consumer<T> consumer) {
        if (spawnMethod == null) return false;
        
        try {
            Entity entity = (Entity) spawnMethod.invoke(location.getWorld(), location, entityClass, consumer);
            boolean valid = entity.isValid();
            entity.remove();
            return valid;
        } catch(Throwable e) { 
            String entityName = entityClass.getSimpleName();
            gSitMain.getLogger().log(Level.SEVERE, "Could not validate " + entityName + " location", e);
            return false;
        }
    }

    @Override
    public Entity createSeatEntity(Location location, Entity entity, boolean canRotate) {
        if (spawnMethod == null) return null;
        
        try {
            final boolean[] riding = { true };
            Consumer<ArmorStand> consumer = armorStand -> {
                setArmorStandFlags(armorStand);
                armorStand.addScoreboardTag(GSitMain.NAME + "_SeatEntity");
                
                if (!enhancedCompatibility && entity != null && entity.isValid()) {
                    riding[0] = armorStand.addPassenger(entity);
                }
            };

            Entity seatEntity = (Entity) spawnMethod.invoke(location.getWorld(), location, ArmorStand.class, consumer);
            
            if (enhancedCompatibility && entity != null && entity.isValid()) {
                riding[0] = seatEntity.addPassenger(entity);
            }
            
            if (entity != null && entity.isValid() && (!riding[0] || !seatEntity.getPassengers().contains(entity))) {
                seatEntity.remove();
                return null;
            }

            return seatEntity;
        } catch(Throwable e) { 
            gSitMain.getLogger().log(Level.SEVERE, "Could not create seat entity", e);
            return null;
        }
    }

    private void setArmorStandFlags(ArmorStand armorStand) {
        try { armorStand.setInvisible(true); } 
        catch(Throwable e) { 
            try { armorStand.setVisible(false); } catch(Throwable ignored) { } 
        }
        try { armorStand.setGravity(false); } catch(Throwable ignored) { }
        try { armorStand.setMarker(true); } catch(Throwable ignored) { }
        try { armorStand.setInvulnerable(true); } catch(Throwable ignored) { }
        try { armorStand.setSmall(true); } catch(Throwable ignored) { }
        try { armorStand.setBasePlate(false); } catch(Throwable ignored) { }
    }

    @Override
    public Set<UUID> createPlayerSeatEntities(Player player, Player target) {
        if (spawnMethod == null || player == null || !player.isValid()) return Collections.emptySet();
        
        Entity lastEntity = target;
        Set<UUID> playerSeatEntityIds = new HashSet<>(seatEntityStackCount);
        
        try {
            for (int entityCount = 1; entityCount <= seatEntityStackCount; entityCount++) {
                Entity finalLastEntity = lastEntity;
                boolean isLast = (entityCount == seatEntityStackCount);
                
                Consumer<AreaEffectCloud> consumer = cloud -> {
                    setCloudFlags(cloud);
                    finalLastEntity.addPassenger(cloud);
                    if (isLast) cloud.addPassenger(player);
                };
                
                lastEntity = (Entity) spawnMethod.invoke(
                    target.getWorld(), 
                    finalLastEntity.getLocation(), 
                    AreaEffectCloud.class, 
                    consumer
                );
                playerSeatEntityIds.add(lastEntity.getUniqueId());
            }
            return playerSeatEntityIds;
        } catch(Throwable e) { 
            gSitMain.getLogger().log(Level.SEVERE, "Could not create player seat entities", e);
            return Collections.emptySet();
        }
    }

    private void setCloudFlags(AreaEffectCloud cloud) {
        try { cloud.setRadius(0); } catch(Throwable ignored) { }
        try { cloud.setDuration(Integer.MAX_VALUE); } catch(Throwable ignored) { }
        try { cloud.setGravity(false); } catch(Throwable ignored) { }
        try { cloud.setInvulnerable(true); } catch(Throwable ignored) { }
        cloud.addScoreboardTag(PlayerSitService.PLAYERSIT_ENTITY_TAG);
    }

    @Override
    public IGPose createPose(GSeat seat, Pose pose) { return null; }

    @Override
    public IGCrawl createCrawl(Player player) { return null; }
}
