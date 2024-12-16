package dev.geco.gsit.mcv.v1_20_5.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.*;

import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;

public class BoxEntity extends Shulker {

    public BoxEntity(Location Location) {

        super(EntityType.SHULKER, ((CraftWorld) Location.getWorld()).getHandle());
        setPos(Location.getX(), Location.getY(), Location.getZ());

        persist = false;

        setInvisible(true);
        setNoGravity(true);
        setInvulnerable(true);
        setNoAi(true);
        setSilent(true);
        setAttachFace(Direction.UP);
    }

    @Override
    public boolean canChangeDimensions() { return false; }

    @Override
    public boolean isAffectedByFluids() { return false; }

}