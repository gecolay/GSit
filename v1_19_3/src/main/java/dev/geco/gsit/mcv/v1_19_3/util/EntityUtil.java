package dev.geco.gsit.mcv.v1_19_3.util;

import java.util.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R2.*;
import org.bukkit.craftbukkit.v1_19_R2.entity.*;
import org.bukkit.entity.*;

import net.minecraft.network.protocol.game.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.mcv.v1_19_3.objects.*;
import dev.geco.gsit.objects.*;
import dev.geco.gsit.util.*;

public class EntityUtil implements IEntityUtil {

    private final GSitMain GPM = GSitMain.getInstance();

    @Override
    public void posEntity(Entity Entity, Location Location) {

        if(Entity instanceof Player) {

            ((CraftEntity) Entity).getHandle().setPos(Location.getX(), Location.getY(), Location.getZ());
            ((CraftPlayer) Entity).getHandle().connection.send(new ClientboundPlayerPositionPacket(Location.getX(), Location.getY(), Location.getZ(), Location.getYaw(), Location.getPitch(), ClientboundPlayerPositionPacket.RelativeArgument.unpack(0), 0, true));
        } else ((CraftEntity) Entity).getHandle().moveTo(Location.getX(), Location.getY(), Location.getZ(), Location.getYaw(), Location.getPitch());
    }

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

        boolean spawn = spawnEntity(Location.getWorld(), seatEntity);
        if(!spawn) return null;

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

            boolean spawn = spawnEntity(Holder.getWorld(), playerSeatEntity);

            if(spawn) lastEntity = playerSeatEntity;
        }

        return lastEntity.getUUID();
    }

    private boolean spawnEntity(World Level, net.minecraft.world.entity.Entity Entity) {

        if(!GPM.supportsPaperFeature()) {

            try {

                ((CraftWorld) Level).getHandle().entityManager.addNewEntity(Entity);
                return true;
            } catch (Throwable ignored) { }
        }

        try {

            net.minecraft.world.level.entity.LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter = ((CraftWorld) Level).getHandle().getEntities();
            levelEntityGetter.getClass().getMethod("addNewEntity", net.minecraft.world.entity.Entity.class).invoke(levelEntityGetter, Entity);
            return true;
        } catch (Throwable ignored) { }

        return false;
    }

    @Override
    public IGPoseSeat createPoseSeatObject(GSeat Seat, Pose Pose) { return new GPoseSeat(Seat, Pose); }

    @Override
    public IGCrawl createCrawlObject(Player Player) { return new GCrawl(Player); }

}