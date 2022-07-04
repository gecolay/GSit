package dev.geco.gsit.util;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.manager.*;

public class TeleportUtil implements ITeleportUtil {

    public void teleportEntity(Entity Entity, Location Location) { }

    public void teleportEntity(Entity Entity, Location Location, boolean Dismount) { }

    public void posEntity(Entity Entity, Location Location) {

        try {

            Object entity = NMSManager.getHandle(Entity);

            NMSManager.getMethod("setPosition", entity.getClass(), double.class, double.class, double.class).invoke(entity, Location.getX(), Location.getY(), Location.getZ());
        } catch (Exception ignored) { }
    }

}