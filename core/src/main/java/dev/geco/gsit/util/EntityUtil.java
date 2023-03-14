package dev.geco.gsit.util;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;

public class EntityUtil implements IEntityUtil {

    private final GSitMain GPM = GSitMain.getInstance();

    public void posEntity(Entity Entity, Location Location) {

        try {

            Object entity = NMSManager.getHandle(Entity);

            NMSManager.getMethod("setPosition", entity.getClass(), double.class, double.class, double.class).invoke(entity, Location.getX(), Location.getY(), Location.getZ());
        } catch (Exception ignored) { }
    }

    public boolean isLocationValid(Location Location) {

        Entity seatEntity = Location.getWorld().spawn(Location, ArmorStand.class, armorStand -> {

            try { armorStand.setInvisible(true); } catch (Error e) { try { NMSManager.getMethod("setVisible", armorStand.getClass(), boolean.class).invoke(armorStand, false); } catch (Exception | Error ignored) { } }
            try { armorStand.setMarker(true); } catch (Error ignored) { }
        });

        boolean valid = seatEntity.isValid();

        seatEntity.remove();

        return valid;
    }

    public boolean isPlayerSitLocationValid(Entity Holder) {

        Entity playerSeatEntity = Holder.getWorld().spawn(Holder.getLocation(), AreaEffectCloud.class, areaEffectCloud -> {

            try { areaEffectCloud.setRadius(0); } catch (Exception ignored) { }
            try { areaEffectCloud.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData()); } catch (Exception ignored) { }
            try { areaEffectCloud.setWaitTime(0); } catch (Exception ignored) { }
        });

        boolean valid = playerSeatEntity.isValid();

        playerSeatEntity.remove();

        return valid;
    }

    public Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate) {

        final boolean[] riding = { true };

        Entity seatEntity = Location.getWorld().spawn(Location, ArmorStand.class, armorStand -> {

            try { armorStand.setInvisible(true); } catch (Error e) { try { NMSManager.getMethod("setVisible", armorStand.getClass(), boolean.class).invoke(armorStand, false); } catch (Exception | Error ignored) { } }
            try { armorStand.setGravity(false); } catch (Error ignored) { }
            try { armorStand.setMarker(true); } catch (Error ignored) { }
            try { armorStand.setInvulnerable(true); } catch (Error ignored) { }
            try { armorStand.setSmall(true); } catch (Error ignored) { }
            try { armorStand.setBasePlate(false); } catch (Error ignored) { }

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

                try { areaEffectCloud.setRadius(0); } catch (Exception ignored) { }
                try { areaEffectCloud.setGravity(false); } catch (Exception ignored) { }
                try { areaEffectCloud.setInvulnerable(true); } catch (Exception ignored) { }
                try { areaEffectCloud.setDuration(Integer.MAX_VALUE); } catch (Exception ignored) { }
                try { areaEffectCloud.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData()); } catch (Exception ignored) { }
                try { areaEffectCloud.setWaitTime(0); } catch (Exception ignored) { }

                areaEffectCloud.setMetadata(GPM.NAME + "A", new FixedMetadataValue(GPM, finalLastEntity));

                finalLastEntity.addPassenger(areaEffectCloud);
                if(finalEntityCount == maxEntities) areaEffectCloud.addPassenger(Rider);
            });
        }
    }

}