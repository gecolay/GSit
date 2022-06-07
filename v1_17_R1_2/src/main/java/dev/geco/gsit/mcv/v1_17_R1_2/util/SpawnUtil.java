package dev.geco.gsit.mcv.v1_17_R1_2.util;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;

import dev.geco.gsit.objects.*;
import dev.geco.gsit.mcv.v1_17_R1_2.objects.*;

public class SpawnUtil implements ISpawnUtil {

    public boolean needCheck() { return false; }

    public boolean checkLocation(Location Location) { return true; }

    public boolean checkPlayerLocation(Entity Holder) {

        if(!needCheck()) return true;

        Entity e = createPlayerSeatEntity(Holder, null);

        boolean v = e.isValid();

        e.remove();

        return v;
    }

    public Entity createSeatEntity(Location Location, Entity Rider) {

        SeatEntity sas = new SeatEntity(Location);

        if(Rider != null && Rider.isValid()) ((CraftEntity) Rider).getHandle().startRiding(sas, true);

        ((CraftWorld) Location.getWorld()).getHandle().entityManager.addNewEntity(sas);

        return sas.getBukkitEntity();
    }

    public Entity createPlayerSeatEntity(Entity Holder, Entity Rider) {

        return Holder.getWorld().spawn(Holder.getLocation(), AreaEffectCloud.class, b -> {
            try { b.setRadius(0); } catch(Exception ignored) { }
            try { b.setGravity(false); } catch(Exception ignored) { }
            try { b.setInvulnerable(true); } catch(Exception ignored) { }
            try { b.setDuration(Integer.MAX_VALUE); } catch(Exception ignored) { }
            try { b.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData()); } catch(Exception ignored) { }
            try { b.setWaitTime(0); } catch(Exception ignored) { }
            if(Rider != null && Rider.isValid()) {
                Holder.addPassenger(b);
                b.addPassenger(Rider);
            }
        });
    }

}