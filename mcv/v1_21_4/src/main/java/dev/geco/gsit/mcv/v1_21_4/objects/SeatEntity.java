package dev.geco.gsit.mcv.v1_21_4.objects;

import java.util.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.*;

import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.phys.*;

public class SeatEntity extends ArmorStand {

    private boolean rotate = false;
    private Callback callback = null;

    public SeatEntity(Location Location) {

        super(((CraftWorld) Location.getWorld()).getHandle(), Location.getX(), Location.getY(), Location.getZ());

        persist = false;

        setInvisible(true);
        setNoGravity(true);
        setMarker(true);
        setInvulnerable(true);
        setSmall(true);
        setNoBasePlate(true);
        setRot(Location.getYaw(), Location.getPitch());
        yRotO = getYRot();
        setYBodyRot(yRotO);
        Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(1f);
        addTag("GSit_SeatEntity");
    }

    public void startRotate() { rotate = true; }

    @Override
    public void tick() {

        if(isAlive() && valid && rotate) {

            Entity rider = getFirstPassenger();

            if(rider == null) return;

            setYRot(rider.getYRot());
            yRotO = getYRot();
        }

        if(callback != null) callback.call();
    }

    public void setCallback(Callback Callback) { callback = Callback; }

    @Override
    public void move(MoverType MoverType, Vec3 Vec3) { }

    @Override
    public boolean hurtServer(ServerLevel ServerLevel, DamageSource DamageSource, float Damage) { return false; }

    @Override
    protected void handlePortal() { }

    @Override
    public boolean isAffectedByFluids() { return false; }

    @Override
    public boolean dismountsUnderwater() { return false; }

    public interface Callback { void call(); }

}