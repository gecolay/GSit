package dev.geco.gsit.objects;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

import java.util.concurrent.TimeUnit;

public class GSeat {

    protected Block block;

    protected Location location;

    protected final LivingEntity entity;

    protected final Entity seatEntity;

    protected Location returnLocation;

    private final long spawnTime = System.nanoTime();

    public GSeat(Block Block, Location Location, LivingEntity Entity, Entity SeatEntity, Location ReturnLocation) {

        block = Block;
        location = Location;
        entity = Entity;
        seatEntity = SeatEntity;
        returnLocation = ReturnLocation;
    }

    public Block getBlock() { return block; }

    public GSeat setBlock(Block Block) {

        block = Block;
        return this;
    }

    public Location getLocation() { return location.clone(); }

    public GSeat setLocation(Location Location) {

        location = Location;
        return this;
    }

    public LivingEntity getEntity() { return entity; }

    public Entity getSeatEntity() { return seatEntity; }

    public Location getReturn() { return returnLocation.clone(); }

    public GSeat setReturn(Location ReturnLocation) {

        returnLocation = ReturnLocation;
        return this;
    }

    public long getSeconds() { return (System.nanoTime() - spawnTime) / 1_000_000_000; }

    public String toString() { return seatEntity.getUniqueId().toString(); }

}