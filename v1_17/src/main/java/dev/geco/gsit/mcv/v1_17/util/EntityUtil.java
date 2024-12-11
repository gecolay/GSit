package dev.geco.gsit.mcv.v1_17.util;

import java.lang.reflect.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.*;
import org.bukkit.craftbukkit.v1_17_R1.entity.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_17.objects.*;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.util.*;

public class EntityUtil implements IEntityUtil {

    private final GSitMain GPM = GSitMain.getInstance();

    @Override
    public void setEntityLocation(Entity Entity, Location Location) { ((CraftEntity) Entity).getHandle().moveTo(Location.getX(), Location.getY(), Location.getZ(), Location.getYaw(), Location.getPitch()); }

    @Override
    public boolean isLocationValid(Location Location) { return true; }

    @Override
    public boolean isPlayerSitLocationValid(Entity Holder) {

        try {

            World world = Holder.getWorld();
            Method spawn = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);

            org.bukkit.util.Consumer<AreaEffectCloud> consumer = (areaEffectCloud) -> {

                try { areaEffectCloud.setRadius(0); } catch (Throwable ignored) { }
                try { areaEffectCloud.setWaitTime(0); } catch (Throwable ignored) { }
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

        if(Rider == null || !Rider.isValid()) return null;

        boolean riding = true;

        net.minecraft.world.entity.Entity rider = ((CraftEntity) Rider).getHandle();

        SeatEntity seatEntity = new SeatEntity(Location);

        if(!GPM.getCManager().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);

        ((CraftWorld) Location.getWorld()).getHandle().entityManager.addNewEntity(seatEntity);

        if(GPM.getCManager().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);

        if(!riding || !seatEntity.passengers.contains(rider)) {

            seatEntity.discard();
            return null;
        }

        if(Rotate) seatEntity.startRotate();

        return seatEntity.getBukkitEntity();
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
                    try { areaEffectCloud.setDuration(Integer.MAX_VALUE); } catch (Throwable ignored) { }
                    try { areaEffectCloud.setWaitTime(0); } catch (Throwable ignored) { }
                    areaEffectCloud.addScoreboardTag(GPM.NAME + "_PlayerSeatEntity");
                    finalLastEntity.addPassenger(areaEffectCloud);
                    if(finalEntityCount == maxEntities) areaEffectCloud.addPassenger(Rider);
                };

                lastEntity = (Entity) spawn.invoke(world, finalLastEntity.getLocation(), AreaEffectCloud.class, consumer);
            }
        } catch (Throwable e) { e.printStackTrace(); }

        return lastEntity.getUniqueId();
    }

    @Override
    public IGPoseSeat createPoseSeatObject(GSeat Seat, Pose Pose) { return new GPoseSeat(Seat, Pose); }

    @Override
    public IGCrawl createCrawlObject(Player Player) { return new GCrawl(Player); }

}