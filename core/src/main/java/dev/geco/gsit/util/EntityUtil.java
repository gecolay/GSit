package dev.geco.gsit.util;

import dev.geco.gsit.GSitMain;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class EntityUtil {

    private final GSitMain GPM;

    public EntityUtil(GSitMain GPluginMain) { GPM = GPluginMain; }

    public boolean canSpawn(Location Location) {

        ArmorStand sa = Location.getWorld().spawn(Location, ArmorStand.class, b -> {
            try { b.setInvisible(true); } catch(Exception ignored) { }
            try { b.setSmall(true); } catch(Exception ignored) { }
            try { b.setGravity(false); } catch(Exception ignored) { }
            try { b.setMarker(true); } catch(Exception ignored) { }
            try { b.setBasePlate(false); } catch(Exception ignored) { }
            try { b.setInvulnerable(true); } catch(Exception ignored) { }
        });

        return sa.isValid();
    }

}