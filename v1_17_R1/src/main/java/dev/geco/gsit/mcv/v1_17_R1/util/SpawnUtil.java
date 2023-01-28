package dev.geco.gsit.mcv.v1_17_R1.util;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.*;
import org.bukkit.craftbukkit.v1_17_R1.entity.*;
import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.util.*;
import dev.geco.gsit.mcv.v1_17_R1.objects.*;

public class SpawnUtil implements ISpawnUtil {

    private final GSitMain GPM = GSitMain.getInstance();

    private final dev.geco.gsit.util.SpawnUtil spawnUtil = new dev.geco.gsit.util.SpawnUtil();

    public boolean needCheck() { return false; }

    public boolean checkLocation(Location Location) { return true; }

    public boolean checkPlayerLocation(Entity Holder) { return spawnUtil.checkPlayerLocation(Holder); }

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

    public void createPlayerSeatEntity(Entity Holder, Entity Rider) { spawnUtil.createPlayerSeatEntity(Holder, Rider); }

}