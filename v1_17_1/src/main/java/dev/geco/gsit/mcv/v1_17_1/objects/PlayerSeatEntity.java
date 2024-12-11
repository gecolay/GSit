package dev.geco.gsit.mcv.v1_17_1.objects;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.*;
import org.bukkit.craftbukkit.v1_17_R1.entity.*;

import net.minecraft.core.particles.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.*;
import net.minecraft.util.*;

import net.minecraft.world.entity.*;

public class PlayerSeatEntity extends Entity {

    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(PlayerSeatEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(PlayerSeatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(PlayerSeatEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(PlayerSeatEntity.class, EntityDataSerializers.PARTICLE);

    public PlayerSeatEntity(Location Location) {

        super(EntityType.AREA_EFFECT_CLOUD, ((CraftWorld) Location.getWorld()).getHandle());

        persist = false;
        setPos(Location.getX(), Location.getY(), Location.getZ());

        setRadius(0);
        setNoGravity(true);
        setInvulnerable(true);
        addTag("GSit_PlayerSeatEntity");
    }

    @Override
    protected void defineSynchedData() {
        getEntityData().define(DATA_COLOR, 0);
        getEntityData().define(DATA_RADIUS, 0.5F);
        getEntityData().define(DATA_WAITING, false);
        getEntityData().define(DATA_PARTICLE, ParticleTypes.ENTITY_EFFECT);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) { }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) { }

    @Override
    public Packet<?> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }

    @Override
    public CraftEntity getBukkitEntity() {
        return new CraftEntity(level.getCraftServer(), this) {
            @Override
            public org.bukkit.entity.EntityType getType() { return org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD; }
        };
    }

    public void setRadius(float Radius) { getEntityData().set(DATA_RADIUS, Mth.clamp(Radius, 0.0F, 32.0F)); }

    @Override
    public void tick() { }

    @Override
    public boolean canChangeDimensions() { return false; }

    @Override
    public boolean rideableUnderWater() { return true; }

}