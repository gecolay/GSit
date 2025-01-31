package dev.geco.gsit.object;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class GSeat {

    protected Block block;
    protected Location location;
    protected final LivingEntity entity;
    protected final Entity seatEntity;
    protected Location returnLocation;
    private final long spawnTime = System.nanoTime();

    public GSeat(@NotNull Block block, @NotNull Location location, @NotNull LivingEntity entity, @NotNull Entity seatEntity, @NotNull Location returnLocation) {
        this.block = block;
        this.location = location;
        this.entity = entity;
        this.seatEntity = seatEntity;
        this.returnLocation = returnLocation;
    }

    public @NotNull Block getBlock() { return block; }

    public @NotNull GSeat setBlock(@NotNull Block Block) {
        block = Block;
        return this;
    }

    public @NotNull Location getLocation() { return location.clone(); }

    public @NotNull GSeat setLocation(@NotNull Location Location) {
        location = Location;
        return this;
    }

    public @NotNull LivingEntity getEntity() { return entity; }

    public @NotNull Entity getSeatEntity() { return seatEntity; }

    public @NotNull Location getReturnLocation() { return returnLocation.clone(); }

    public @NotNull GSeat setReturnLocation(@NotNull Location ReturnLocation) {
        returnLocation = ReturnLocation;
        return this;
    }

    public long getLifetimeInNanoSeconds() { return System.nanoTime() - spawnTime; }

    public @NotNull String toString() { return seatEntity.getUniqueId().toString(); }

}