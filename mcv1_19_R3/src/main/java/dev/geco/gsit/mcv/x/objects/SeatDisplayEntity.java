package dev.geco.gsit.mcv.x.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.*;

import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;

public class SeatDisplayEntity extends Interaction {

    public SeatDisplayEntity(Location Location) {

        super(EntityType.INTERACTION, ((CraftWorld) Location.getWorld()).getHandle());
        setPos(Location.getX(), Location.getY(), Location.getZ());

        persist = false;

        setInvisible(true);
        setNoGravity(true);
        setInvulnerable(true);
        setWidth(0);
        setHeight(0);
    }

    public void move(MoverType MoverType, Vec3 Vec3) { }

    public boolean canChangeDimensions() { return false; }

    public boolean dismountsUnderwater() { return false; }

}