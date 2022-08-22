package dev.geco.gsit.mcv.v1_19_R1_2.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.*;

import net.minecraft.core.*;
import net.minecraft.world.damagesource.*;
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

    public void tick() { }

    public boolean damageEntity0(DamageSource DamageSource, float Damage) { return false; }

    public boolean canChangeDimensions() { return false; }

    public boolean isAffectedByFluids() { return false; }

    public boolean rideableUnderWater() { return true; }

}