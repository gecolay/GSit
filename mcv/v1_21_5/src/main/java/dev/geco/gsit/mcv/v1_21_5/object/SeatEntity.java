package dev.geco.gsit.mcv.v1_21_5.object;

import dev.geco.gsit.GSitMain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class SeatEntity extends ArmorStand {

    private boolean rotate = false;
    private Runnable runnable = null;

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
        addTag(GSitMain.NAME + "_" + getClass().getSimpleName());
    }

    public void startRotate() { rotate = true; }

    @Override
    public void tick() {
        if(runnable != null) runnable.run();
        if(!isAlive() || !valid || !rotate) return;
        Entity rider = getFirstPassenger();
        if(rider == null) return;
        setYRot(rider.getYRot());
        yRotO = getYRot();
    }

    public void setRunnable(Runnable runnable) { this.runnable = runnable; }

    @Override
    public void move(@NotNull MoverType moverType, @NotNull Vec3 movement) { }

    @Override
    public boolean hurtServer(@NotNull ServerLevel world, @NotNull DamageSource source, float amount) { return false; }

    @Override
    protected void handlePortal() { }

    @Override
    public boolean isAffectedByFluids() { return false; }

    @Override
    public boolean dismountsUnderwater() { return false; }

}