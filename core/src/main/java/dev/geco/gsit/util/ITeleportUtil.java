package dev.geco.gsit.util;

import org.bukkit.*;
import org.bukkit.entity.*;

public interface ITeleportUtil {

    void teleportEntity(Entity Entity, Location Location, boolean Dismount);

    void posEntity(Entity Entity, Location Location);

}