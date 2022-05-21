package dev.geco.gsit.util;

import org.bukkit.Location;
import org.bukkit.entity.*;

import dev.geco.gsit.objects.*;

public class SpawnUtil implements ISpawnUtil {

    public boolean needCheck() { return true; }

    public boolean check(Location Location) {

        if(!needCheck()) return true;

        Entity e = createEntity(Location);

        boolean v = e.isValid();

        e.remove();

        return v;
    }

    public Entity createEntity(Location Location) {
        return createEntity(Location, null);
    }

    public Entity createEntity(Location Location, Entity Rider) {

        return Location.getWorld().spawn(Location, ArmorStand.class, b -> {
            try { b.setInvisible(true); } catch(Exception ignored) { }
            try { b.setGravity(false); } catch(Exception ignored) { }
            try { b.setMarker(true); } catch(Exception ignored) { }
            try { b.setInvulnerable(true); } catch(Exception ignored) { }
            try { b.setSmall(true); } catch(Exception ignored) { }
            try { b.setBasePlate(false); } catch(Exception ignored) { }
            if(Rider != null && Rider.isValid()) b.addPassenger(Rider);
        });
    }

}