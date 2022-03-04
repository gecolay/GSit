package dev.geco.gsit.mcv.v1_18_R2.objects;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

public class SeatArmorStand extends ArmorStand {

    public SeatArmorStand(Level world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    public boolean canChangeDimensions() { return false; }

    public boolean isAffectedByFluids() { return false; }

    public boolean isSensitiveToWater() { return false; }

    public boolean rideableUnderWater() { return true; }

}