package dev.geco.gsit.mcv.v1_17.util;

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
    public boolean isPlayerSitLocationValid(Entity Holder) { return true; }

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

        net.minecraft.world.entity.Entity lastEntity = ((CraftEntity) Holder).getHandle();

        int maxEntities = GPM.getPlayerSitManager().getSeatEntityCount();

        if(maxEntities == 0) {

            ((CraftEntity) Rider).getHandle().startRiding(lastEntity, true);
            return null;
        }

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {

            net.minecraft.world.entity.Entity playerSeatEntity = new PlayerSeatEntity(Holder.getLocation());

            playerSeatEntity.startRiding(lastEntity, true);

            if(entityCount == maxEntities) ((CraftEntity) Rider).getHandle().startRiding(playerSeatEntity, true);

            ((CraftWorld) Holder.getWorld()).getHandle().entityManager.addNewEntity(playerSeatEntity);

            lastEntity = playerSeatEntity;
        }

        return lastEntity.getUUID();
    }

    @Override
    public IGPoseSeat createPoseSeatObject(GSeat Seat, Pose Pose) { return new GPoseSeat(Seat, Pose); }

    @Override
    public IGCrawl createCrawlObject(Player Player) { return new GCrawl(Player); }

}