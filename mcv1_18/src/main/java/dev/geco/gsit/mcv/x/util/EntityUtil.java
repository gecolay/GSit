package dev.geco.gsit.mcv.x.util;

import java.util.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_18_R1.*;
import org.bukkit.craftbukkit.v1_18_R1.entity.*;
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
            ((CraftPlayer) Entity).getHandle().connection.send(new ClientboundPlayerPositionPacket(Location.getX(), Location.getY(), Location.getZ(), Location.getYaw(), Location.getPitch(), ClientboundPlayerPositionPacket.RelativeArgument.unpack(0), 0, true));
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

        ((CraftWorld) Location.getWorld()).getHandle().entityManager.addNewEntity(seatEntity);

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

            net.minecraft.world.entity.Entity playerSeatEntity = new PlayerSeatEntity(Holder.getLocation());

            playerSeatEntity.startRiding(lastEntity, true);

            if(entityCount == maxEntities) ((CraftEntity) Rider).getHandle().startRiding(playerSeatEntity, true);

            ((CraftWorld) Holder.getWorld()).getHandle().entityManager.addNewEntity(playerSeatEntity);

            lastEntity = playerSeatEntity;
        }

        return lastEntity.getUUID();
    }

    public IGPoseSeat createPoseSeatObject(GSeat Seat, Pose Pose) { return new GPoseSeat(Seat, Pose); }

    public IGCrawl createCrawlObject(Player Player) { return new GCrawl(Player); }

}