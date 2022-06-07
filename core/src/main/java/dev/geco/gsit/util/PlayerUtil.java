package dev.geco.gsit.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

import dev.geco.gsit.manager.*;
import dev.geco.gsit.objects.*;

public class PlayerUtil implements IPlayerUtil {

    public void teleport(Player P, Location L) { }

    public void teleport(Player P, Location L, boolean D) { }

    public void pos(org.bukkit.entity.Entity E, Location L) {

        try {

            Object sa = NMSManager.getHandle(E);

            NMSManager.getMethod("setPosition", sa.getClass(), double.class, double.class, double.class).invoke(sa, L.getX(), L.getY(), L.getZ());

        } catch(Exception e) { e.printStackTrace(); }
    }

    public void send(Player P, BaseComponent... M) { P.spigot().sendMessage(ChatMessageType.ACTION_BAR, M); }

}