package dev.geco.gsit.mcv.v1_20_5.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.*;

import net.minecraft.world.entity.*;

public class PlayerSeatEntity extends AreaEffectCloud {

    public PlayerSeatEntity(Location Location) {

        super(((CraftWorld) Location.getWorld()).getHandle(), Location.getX(), Location.getY(), Location.getZ());

        persist = false;

        setRadius(0);
        setNoGravity(true);
        setInvulnerable(true);
        addTag("GSit_PlayerSeatEntity");
    }

    @Override
    public void tick() { }

    @Override
    public boolean canChangeDimensions() { return false; }

    @Override
    public boolean dismountsUnderwater() { return false; }

}