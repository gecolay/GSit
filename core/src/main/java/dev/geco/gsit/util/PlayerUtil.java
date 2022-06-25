package dev.geco.gsit.util;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.manager.*;

public class PlayerUtil implements IPlayerUtil {

    public void teleport(Player P, Location L) { }

    public void teleport(Player P, Location L, boolean D) { }

    public void pos(org.bukkit.entity.Entity E, Location L) {

        try {

            Object sa = NMSManager.getHandle(E);

            NMSManager.getMethod("setPosition", sa.getClass(), double.class, double.class, double.class).invoke(sa, L.getX(), L.getY(), L.getZ());

        } catch (Exception e) { e.printStackTrace(); }
    }

}