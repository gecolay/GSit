package dev.geco.gsit.mcv.v1_19_4.objects;

import dev.geco.gsit.GSitMain;
import net.minecraft.world.entity.AreaEffectCloud;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;

public class PlayerSeatEntity extends AreaEffectCloud {

    public PlayerSeatEntity(Location location) {
        super(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ());
        persist = false;
        setRadius(0);
        setNoGravity(true);
        setInvulnerable(true);
        addTag(GSitMain.NAME + "_" + getClass().getSimpleName());
    }

    @Override
    public void tick() { }

    @Override
    public boolean canChangeDimensions() { return false; }

    @Override
    public boolean dismountsUnderwater() { return false; }

}