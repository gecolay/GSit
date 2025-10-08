package dev.geco.gsit.mcv.v1_18_2.entity;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;

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