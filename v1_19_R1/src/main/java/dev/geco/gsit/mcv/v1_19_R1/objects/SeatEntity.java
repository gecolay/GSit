package dev.geco.gsit.mcv.v1_19_R1.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.*;

import net.minecraft.world.entity.decoration.*;

public class SeatEntity extends ArmorStand {

    public SeatEntity(Location Location) {

        super(((CraftWorld) Location.getWorld()).getHandle(), Location.getX(), Location.getY(), Location.getZ());

        persist = false;

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