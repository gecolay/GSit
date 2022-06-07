package dev.geco.gsit.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;

import dev.geco.gsit.manager.*;
import dev.geco.gsit.objects.*;

public class SpawnUtil implements ISpawnUtil {

    public boolean needCheck() { return true; }

    public boolean checkLocation(Location Location) {

        if(!needCheck()) return true;

        Entity e = createSeatEntity(Location, null);

        boolean v = e.isValid();

        e.remove();

        return v;
    }

    public boolean checkPlayerLocation(Entity Holder) {

        if(!needCheck()) return true;

        Entity e = createPlayerSeatEntity(Holder, null);

        boolean v = e.isValid();

        e.remove();

        return v;
    }

    public Entity createSeatEntity(Location Location, Entity Rider) {

        return Location.getWorld().spawn(Location, ArmorStand.class, b -> {
            try { b.setInvisible(true); } catch(Error ignored) {
                try { NMSManager.getMethod("setVisible", b.getClass(), boolean.class).invoke(b, false); } catch(Exception | Error ignored1) { }
            }
            try { b.setGravity(false); } catch(Error ignored) { }
            try { b.setMarker(true); } catch(Error ignored) { }
            try { b.setInvulnerable(true); } catch(Error ignored) { }
            try { b.setSmall(true); } catch(Error ignored) { }
            try { b.setBasePlate(false); } catch(Error ignored) { }
            if(Rider != null && Rider.isValid()) b.addPassenger(Rider);
        });
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