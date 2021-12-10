package dev.geco.gsit.objects;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ITeleportUtil {

    void teleport(Player P, Location L);

    void teleport(Player P, Location L, boolean D);

}