package dev.geco.gsit.objects;

import org.bukkit.Location;
import org.bukkit.entity.*;

public interface ITeleportUtil {

    void teleport(Player P, Location L);

    void teleport(Player P, Location L, boolean D);

    void pos(Entity E, Location L);

}