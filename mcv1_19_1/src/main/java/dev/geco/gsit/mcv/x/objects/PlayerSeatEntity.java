package dev.geco.gsit.mcv.x.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.*;

import net.minecraft.core.particles.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.*;

public class PlayerSeatEntity extends AreaEffectCloud {

    public PlayerSeatEntity(Location Location) {

        super(((CraftWorld) Location.getWorld()).getHandle(), Location.getX(), Location.getY(), Location.getZ());

        persist = false;

        setRadius(0);
        setNoGravity(true);
        setInvulnerable(true);
        setDuration(Integer.MAX_VALUE);
        setParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AIR.defaultBlockState()));
        setWaitTime(0);
        addTag("GSit_PlayerSeatEntity");
    }

    public void tick() { }

    public boolean canChangeDimensions() { return false; }

    public boolean rideableUnderWater() { return true; }

}