package dev.geco.gsit.mcv.x.util;

import java.util.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_21_R2.*;
import org.bukkit.craftbukkit.v1_21_R2.entity.*;
import org.bukkit.entity.*;

import net.minecraft.network.protocol.game.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.util.*;
import dev.geco.gsit.mcv.x.objects.*;
import dev.geco.gsit.objects.*;

public class EntityUtil implements IEntityUtil {

    private final GSitMain GPM = GSitMain.getInstance();
    protected final HashMap<Integer, Entity> playerMap = new HashMap<>();

    public HashMap<Integer, Entity> getSeatMap() { return playerMap; }

    public void posEntity(org.bukkit.entity.Entity Entity, Location Location) {

        if(Entity instanceof Player) {

            ((CraftEntity) Entity).getHandle().setPos(Location.getX(), Location.getY(), Location.getZ());
            ((CraftPlayer) Entity).getHandle().connection.send(new ClientboundPlayerPositionPacket(Entity.getEntityId(), net.minecraft.world.entity.PositionMoveRotation.of(((CraftPlayer) Entity).getHandle()), Set.of()));
        } else ((CraftEntity) Entity).getHandle().moveTo(Location.getX(), Location.getY(), Location.getZ(), Location.getYaw(), Location.getPitch());
    }

    public boolean isLocationValid(Location Location) { return true; }

    public boolean isPlayerSitLocationValid(Entity Holder) { return true; }

    public Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate) {

        if(Rider == null || !Rider.isValid()) return null;

        boolean riding = true;

        net.minecraft.world.entity.Entity rider = ((CraftEntity) Rider).getHandle();

        SeatEntity seatEntity = new SeatEntity(Location);

        playerMap.put(seatEntity.getId(), Rider);

        if(!GPM.getCManager().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);

        boolean spawn = spawnEntity(Location.getWorld(), seatEntity);

        if(!spawn) {
            playerMap.remove(seatEntity.getId());
            return null;
        }

        if(GPM.getCManager().ENHANCED_COMPATIBILITY) riding = rider.startRiding(seatEntity, true);

        if(!riding || !seatEntity.passengers.contains(rider)) {

            seatEntity.discard();
            playerMap.remove(seatEntity.getId());
            return null;
        }

        if(Rotate) seatEntity.startRotate();

        return seatEntity.getBukkitEntity();
    }

    public void removeSeatEntity(Entity Entity) {
        playerMap.remove(Entity.getEntityId());
        Entity.remove();
    }

    public UUID createPlayerSeatEntity(Entity Holder, Entity Rider) {

        if(Rider == null || !Rider.isValid()) return null;

        net.minecraft.world.entity.Entity lastEntity = ((CraftEntity) Holder).getHandle();

        int maxEntities = GPM.getPlayerSitManager().getSeatEntityCount();

        if(maxEntities == 0) {

            ((CraftEntity) Rider).getHandle().startRiding(lastEntity, true);
            return null;
        }

        for(int entityCount = 1; entityCount <= maxEntities; entityCount++) {

            PlayerSeatEntity playerSeatEntity = new PlayerSeatEntity(Holder.getLocation());

            playerSeatEntity.setVehicle(lastEntity);

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

    public IGPoseSeat createPoseSeatObject(GSeat Seat, Pose Pose) { return new GPoseSeat(Seat, Pose); }

    public IGCrawl createCrawlObject(Player Player) { return new GCrawl(Player); }

}