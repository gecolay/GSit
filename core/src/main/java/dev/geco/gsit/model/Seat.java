package dev.geco.gsit.model;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class Seat {

    protected Block block;
    protected Location location;
    protected final LivingEntity entity;
    protected final Entity seatEntity;
    protected Location returnLocation;
    private final long spawnTime = System.nanoTime();

    public Seat(@NotNull Block block, @NotNull Location location, @NotNull LivingEntity entity, @NotNull Entity seatEntity, @NotNull Location returnLocation) {
        this.block = block;
        this.location = location;
        this.entity = entity;
        this.seatEntity = seatEntity;
        this.returnLocation = returnLocation;
    }

    public @NotNull Block getBlock() { return block; }

    public @NotNull Seat setBlock(@NotNull Block block) {
        this.block = block;
        return this;
    }

    public @NotNull Location getLocation() { return location.clone(); }

    public @NotNull Seat setLocation(@NotNull Location location) {
        this.location = location;
        return this;
    }

    public @NotNull LivingEntity getEntity() { return entity; }

    public @NotNull Entity getSeatEntity() { return seatEntity; }

    public @NotNull Location getReturnLocation() { return returnLocation.clone(); }

    public @NotNull Seat setReturnLocation(@NotNull Location returnLocation) {
        this.returnLocation = returnLocation;
        return this;
    }

    public long getLifetimeInNanoSeconds() { return System.nanoTime() - spawnTime; }

    public @NotNull String toString() { return seatEntity.getUniqueId().toString(); }

}