package dev.geco.gsit.mcv.v1_19.entity;

import dev.geco.gsit.service.SitService;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class SeatEntity extends ArmorStand {

    private boolean rotate = false;

    public SeatEntity(Location location) {
        super(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ());
        persist = false;
        setInvisible(true);
        setNoGravity(true);
        setMarker(true);
        setInvulnerable(true);
        setSmall(true);
        setNoBasePlate(true);
        setRot(location.getYaw(), location.getPitch());
        yRotO = getYRot();
        setYBodyRot(yRotO);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(1f);
        addTag(SitService.SIT_TAG);
    }

    public void startRotate() { rotate = true; }

    @Override
    public void tick() {
        if(!isAlive() || !valid || !rotate) return;
        Entity rider = getFirstPassenger();
        if(rider == null) return;
        setYRot(rider.getYRot());
        yRotO = getYRot();
    }

    @Override
    public void move(@NotNull MoverType moverType, @NotNull Vec3 movement) { }

    @Override
    public boolean damageEntity0(@NotNull DamageSource damageSource, float damage) { return false; }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float damage) { return false; }

    @Override
    public boolean canChangeDimensions() { return false; }

    @Override
    public boolean isAffectedByFluids() { return false; }

    @Override
    public boolean rideableUnderWater() { return true; }

}