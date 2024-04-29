package dev.geco.gsit.mcv.x.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.*;

import net.minecraft.world.entity.*;

public class PlayerSeatEntity extends AreaEffectCloud {

    public PlayerSeatEntity(Location Location) {

        super(((CraftWorld) Location.getWorld()).getHandle(), Location.getX(), Location.getY(), Location.getZ());

        persist = false;

        setRadius(0);
        setNoGravity(true);
        setInvulnerable(true);
        setDuration(Integer.MAX_VALUE);
        setWaitTime(0);
        addTag("GSit_PlayerSeatEntity");
    }

    public void tick() { }

    public boolean canChangeDimensions() { return false; }

    public boolean dismountsUnderwater() { return false; }

}