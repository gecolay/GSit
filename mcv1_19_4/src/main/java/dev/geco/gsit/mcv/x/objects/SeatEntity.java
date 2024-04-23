package dev.geco.gsit.mcv.x.objects;

import java.util.*;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.*;

import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.phys.*;

public class SeatEntity extends ArmorStand {

    private boolean rotate = false;

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

    public void tick() {

        if(isAlive() && valid && rotate) {

            Entity rider = getFirstPassenger();

            if(rider == null) return;

            setYRot(rider.getYRot());
            yRotO = getYRot();
        }
    }

    public void move(MoverType MoverType, Vec3 Vec3) { }

    public boolean damageEntity0(DamageSource DamageSource, float Damage) { return false; }

    public boolean hurt(DamageSource DamageSource, float Damage) { return false; }

    public boolean canChangeDimensions() { return false; }

    public boolean isAffectedByFluids() { return false; }

    public boolean dismountsUnderwater() { return false; }

}