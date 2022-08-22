package dev.geco.gsit.mcv.v1_18_R1.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_18_R1.*;

import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;

public class BoxEntity extends Shulker {

    public BoxEntity(Location Location) {

        super(EntityType.SHULKER, ((CraftWorld) Location.getWorld()).getHandle());

        persist = false;

        setInvisible(true);
        setNoGravity(true);
        setInvulnerable(true);
        setNoAi(true);
        setSilent(true);
        setAttachFace(Direction.UP);
    }

    public boolean canChangeDimensions() { return false; }

    public boolean isAffectedByFluids() { return false; }

}