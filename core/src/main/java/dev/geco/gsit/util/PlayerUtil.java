package dev.geco.gsit.util;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.manager.*;

public class PlayerUtil implements IPlayerUtil {

    public void teleportPlayer(Player Player, Location Location) { }

    public void teleportPlayer(Player Player, Location Location, boolean Dismount) { }

    public void teleportEntity(org.bukkit.entity.Entity Entity, Location Location) {

        try {

            Object entity = NMSManager.getHandle(Entity);

            NMSManager.getMethod("setPosition", entity.getClass(), double.class, double.class, double.class).invoke(entity, Location.getX(), Location.getY(), Location.getZ());
        } catch (Exception ignored) { }
    }

}