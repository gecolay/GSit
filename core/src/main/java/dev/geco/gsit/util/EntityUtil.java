package dev.geco.gsit.util;

import java.lang.reflect.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class EntityUtil implements IEntityUtil {

    private final GSitMain GPM;

    public EntityUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public void setEntityLocation(Entity Entity, Location Location) {

        try {

            Method getHandle = Entity.getClass().getMethod("getHandle");
            Object entity = getHandle.invoke(Entity);
            Method setPosition = entity.getClass().getMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
            setPosition.invoke(entity, Location.getX(), Location.getY(), Location.getZ(), Location.getYaw(), Location.getPitch());
        } catch (Throwable e) { e.printStackTrace(); }
    }

    @Override
    public boolean isLocationValid(Location Location) {

        try {

            World world = Location.getWorld();
            Method spawn = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);

            org.bukkit.util.Consumer<ArmorStand> consumer = (armorStand) -> {

                try { armorStand.setInvisible(true); } catch (Throwable e) { try { ArmorStand.class.getMethod("setVisible", boolean.class).invoke(armorStand, false); } catch (Throwable ignored) { } }
                try { armorStand.setMarker(true); } catch (Throwable ignored) { }
            };

            Entity seatEntity = (Entity) spawn.invoke(world, Location, ArmorStand.class, consumer);

            boolean valid = seatEntity.isValid();

            seatEntity.remove();

            return valid;
        } catch (Throwable e) { e.printStackTrace(); }

        return false;
    }

    @Override
    public boolean isPlayerSitLocationValid(Entity Holder) {

        try {

            World world = Holder.getWorld();
            Method spawn = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);

            org.bukkit.util.Consumer<AreaEffectCloud> consumer = (areaEffectCloud) -> {

                try { areaEffectCloud.setRadius(0); } catch (Throwable ignored) { }
            };

            Entity playerSeatEntity = (Entity) spawn.invoke(world, Holder.getLocation(), AreaEffectCloud.class, consumer);

            boolean valid = playerSeatEntity.isValid();

            playerSeatEntity.remove();

            return valid;
        } catch (Throwable e) { e.printStackTrace(); }

        return false;
    }

    @Override
    public Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate) {

        final boolean[] riding = { true };

        try {

            World world = Location.getWorld();
            Method spawn = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);

            org.bukkit.util.Consumer<ArmorStand> consumer = (armorStand) -> {

                try { armorStand.setInvisible(true); } catch (Throwable e) { try { ArmorStand.class.getMethod("setVisible", boolean.class).invoke(armorStand, false); } catch (Throwable ignored) { } }
                try { armorStand.setGravity(false); } catch (Throwable ignored) { }
                try { armorStand.setMarker(true); } catch (Throwable ignored) { }
                try { armorStand.setInvulnerable(true); } catch (Throwable ignored) { }
                try { armorStand.setSmall(true); } catch (Throwable ignored) { }
                try { armorStand.setBasePlate(false); } catch (Throwable ignored) { }
                armorStand.addScoreboardTag(GSitMain.NAME + "_SeatEntity");
                if(!GPM.getCManager().ENHANCED_COMPATIBILITY && Rider != null && Rider.isValid()) riding[0] = armorStand.addPassenger(Rider);
            };

            Entity seatEntity = (Entity) spawn.invoke(world, Location, ArmorStand.class, consumer);

            if(GPM.getCManager().ENHANCED_COMPATIBILITY && Rider != null && Rider.isValid()) riding[0] = seatEntity.addPassenger(Rider);

            if(Rider != null && Rider.isValid() && (!riding[0] || !seatEntity.getPassengers().contains(Rider))) {

                seatEntity.remove();
                return null;
            }

            return seatEntity;
        } catch (Throwable e) { e.printStackTrace(); }

        return null;
    }

    @Override
    public UUID createPlayerSeatEntity(Entity Holder, Entity Rider) {

        if(Rider == null || !Rider.isValid()) return null;

        Entity lastEntity = Holder;

        int maxEntities = GPM.getPlayerSitManager().getSeatEntityCount();

        if(maxEntities == 0) {

            lastEntity.addPassenger(Rider);
            return null;
        }

        try {

            World world = Holder.getWorld();
            Method spawn = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);

            for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {

                Entity finalLastEntity = lastEntity;
                int finalEntityCount = entityCount;

                org.bukkit.util.Consumer<AreaEffectCloud> consumer = (areaEffectCloud) -> {

                    try { areaEffectCloud.setRadius(0); } catch (Throwable ignored) { }
                    try { areaEffectCloud.setGravity(false); } catch (Throwable ignored) { }
                    try { areaEffectCloud.setInvulnerable(true); } catch (Throwable ignored) { }
                    areaEffectCloud.addScoreboardTag(GSitMain.NAME + "_PlayerSeatEntity");
                    finalLastEntity.addPassenger(areaEffectCloud);
                    if(finalEntityCount == maxEntities) areaEffectCloud.addPassenger(Rider);
                };

                lastEntity = (Entity) spawn.invoke(world, finalLastEntity.getLocation(), AreaEffectCloud.class, consumer);
            }
        } catch (Throwable e) { e.printStackTrace(); }

        return lastEntity.getUniqueId();
    }

    @Override
    public IGPoseSeat createPoseSeatObject(GSeat Seat, Pose Pose) { return null; }

    @Override
    public IGCrawl createCrawlObject(Player Player) { return null; }

}