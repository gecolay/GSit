package dev.geco.gsit.util;

import java.lang.reflect.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.objects.*;

public class EntityUtil implements IEntityUtil {

    private final GSitMain GPM = GSitMain.getInstance();

    public void posEntity(Entity Entity, Location Location) {

        try {

            Method getHandle = Entity.getClass().getDeclaredMethod("getHandle");
            Object entity = getHandle.invoke(Entity);
            Method setPosition = entity.getClass().getDeclaredMethod("setPosition", double.class, double.class, double.class);
            setPosition.invoke(entity, Location.getX(), Location.getY(), Location.getZ());
        } catch (Exception ignored) { }
    }

    public boolean isLocationValid(Location Location) {

        Entity seatEntity = Location.getWorld().spawn(Location, ArmorStand.class, armorStand -> {

            try { armorStand.setInvisible(true); } catch (Throwable e) { try { ArmorStand.class.getMethod("setVisible", boolean.class).invoke(armorStand, false); } catch (Throwable ignored) { } }
            try { armorStand.setMarker(true); } catch (Throwable ignored) { }
        });

        boolean valid = seatEntity.isValid();

        seatEntity.remove();

        return valid;
    }

    public boolean isPlayerSitLocationValid(Entity Holder) {

        Entity playerSeatEntity = Holder.getWorld().spawn(Holder.getLocation(), AreaEffectCloud.class, areaEffectCloud -> {

            try { areaEffectCloud.setRadius(0); } catch (Throwable ignored) { }
            try { areaEffectCloud.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData()); } catch (Throwable ignored) { }
            try { areaEffectCloud.setWaitTime(0); } catch (Throwable ignored) { }
        });

        boolean valid = playerSeatEntity.isValid();

        playerSeatEntity.remove();

        return valid;
    }

    public Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate) {

        final boolean[] riding = { true };

        Entity seatEntity = Location.getWorld().spawn(Location, ArmorStand.class, armorStand -> {

            try { armorStand.setInvisible(true); } catch (Throwable e) { try { ArmorStand.class.getMethod("setVisible", boolean.class).invoke(armorStand, false); } catch (Throwable ignored) { } }
            try { armorStand.setGravity(false); } catch (Throwable ignored) { }
            try { armorStand.setMarker(true); } catch (Throwable ignored) { }
            try { armorStand.setInvulnerable(true); } catch (Throwable ignored) { }
            try { armorStand.setSmall(true); } catch (Throwable ignored) { }
            try { armorStand.setBasePlate(false); } catch (Throwable ignored) { }
            armorStand.addScoreboardTag(GPM.NAME + "_SeatEntity");
            if(!GPM.getCManager().ENHANCED_COMPATIBILITY && Rider != null && Rider.isValid()) riding[0] = armorStand.addPassenger(Rider);
        });

        if(GPM.getCManager().ENHANCED_COMPATIBILITY && Rider != null && Rider.isValid()) riding[0] = seatEntity.addPassenger(Rider);

        if(Rider != null && Rider.isValid() && (!riding[0] || !seatEntity.getPassengers().contains(Rider))) {

            seatEntity.remove();
            return null;
        }

        return seatEntity;
    }

    public void createPlayerSeatEntity(Entity Holder, Entity Rider) {

        if(Rider == null || !Rider.isValid()) return;

        Entity lastEntity = Holder;

        int maxEntities = GPM.PLAYER_SIT_SEAT_ENTITIES;

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {

            Entity finalLastEntity = lastEntity;
            int finalEntityCount = entityCount;

            lastEntity = finalLastEntity.getWorld().spawn(finalLastEntity.getLocation(), AreaEffectCloud.class, areaEffectCloud -> {

                try { areaEffectCloud.setRadius(0); } catch (Throwable ignored) { }
                try { areaEffectCloud.setGravity(false); } catch (Throwable ignored) { }
                try { areaEffectCloud.setInvulnerable(true); } catch (Throwable ignored) { }
                try { areaEffectCloud.setDuration(Integer.MAX_VALUE); } catch (Throwable ignored) { }
                try { areaEffectCloud.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData()); } catch (Throwable ignored) { }
                try { areaEffectCloud.setWaitTime(0); } catch (Throwable ignored) { }
                areaEffectCloud.addScoreboardTag(GPM.NAME + "_PlayerSeatEntity");
                finalLastEntity.addPassenger(areaEffectCloud);
                if(finalEntityCount == maxEntities) areaEffectCloud.addPassenger(Rider);
            });
        }
    }

    public IGPoseSeat createPoseSeatObject(GSeat Seat, Pose Pose) { return null; }

    public IGCrawl createCrawlObject(Player Player) { return null; }

}