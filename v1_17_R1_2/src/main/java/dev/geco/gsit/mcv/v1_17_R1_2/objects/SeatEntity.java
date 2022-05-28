package dev.geco.gsit.mcv.v1_17_R1_2.objects;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

import net.minecraft.world.entity.decoration.*;

public class SeatEntity extends ArmorStand {

    public SeatEntity(Location Location) {
        super(((CraftWorld) Location.getWorld()).getHandle(), Location.getX(), Location.getY(), Location.getZ());
        setInvisible(true);
        setNoGravity(true);
        setMarker(true);
        setInvulnerable(true);
        setSmall(true);
        setNoBasePlate(true);
        setRot(Location.getYaw(), Location.getPitch());
    }

    public boolean canChangeDimensions() { return false; }

    public boolean isAffectedByFluids() { return false; }

    public boolean isSensitiveToWater() { return false; }

    public boolean rideableUnderWater() { return true; }

}