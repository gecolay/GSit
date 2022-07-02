package dev.geco.gsit.objects;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;

public class GEmotePart {

    protected final Particle particle;

    protected final long delay;

    protected final long repeat;

    protected final boolean loop;

    protected final int amount;

    protected final double xoffset;
    protected final double yoffset;
    protected final double zoffset;

    protected final double extra;

    protected final Object data;

    public GEmotePart(Particle Particle, long Delay, long Repeat, boolean Loop, int Amount, double XOffset, double YOffset, double ZOffset, double Extra, Object Data) {

        particle = Particle;
        delay = Math.max(Delay, 0);
        repeat = Math.max(Repeat, 1);
        loop = Loop;
        amount = Math.max(Amount, 1);
        xoffset = XOffset;
        yoffset = YOffset;
        zoffset = ZOffset;
        extra = Extra < 0 ? 1.0 : Extra;
        data = Data != null && Particle.getDataType().equals(Data.getClass()) ? Data : null;
    }

    private Vector getCords(LivingEntity Entity) {

        float yaw = Entity.getLocation().getYaw(), yawF = yaw + 180.0f;

        Vector xVector = new Vector(Math.cos(Math.toRadians(yawF)) * getXOffset(), 0, Math.sin(Math.toRadians(yawF)) * getXOffset());

        Vector zVector = new Vector(-Math.sin(Math.toRadians(yaw)) * getZOffset(), 0, Math.cos(Math.toRadians(yaw)) * getZOffset());

        Location location = Entity.getLocation().clone().add(xVector).add(zVector);

        return new Vector(location.getX(), 0, location.getZ());
    }

    protected void start(Player Player, LivingEntity Origin, boolean Head) {

        if(getParticle() == null) return;

        Vector vector = getCords(Origin);

        Player.spawnParticle(getParticle(), vector.getX(), (Head ? Origin.getEyeLocation() : Origin.getLocation()).getY() + getYOffset(), vector.getZ(), getAmount(), 0, 0, 0, getExtra(), getData());
    }

    public Particle getParticle() { return particle; }

    public long getDelay() { return delay; }

    public long getRepeat() { return repeat; }

    public boolean isLoop() { return loop; }

    public int getAmount() { return amount; }

    public double getXOffset() { return xoffset; }

    public double getYOffset() { return yoffset; }

    public double getZOffset() { return zoffset; }

    public double getExtra() { return extra; }

    public Object getData() { return data; }

}