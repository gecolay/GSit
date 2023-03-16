package dev.geco.gsit.mcv.x.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.*;

import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;

public class RotateSeatEntity extends Interaction {

    public RotateSeatEntity(Location Location) {

        super(EntityType.INTERACTION, ((CraftWorld) Location.getWorld()).getHandle());

        persist = false;

        setPos(Location.getX(), Location.getY(), Location.getZ());
        setWidth(0);
        setHeight(0);
    }

    public void move(MoverType MoverType, Vec3 Vec3) { }

    public boolean canChangeDimensions() { return false; }

    public boolean dismountsUnderwater() { return false; }

}