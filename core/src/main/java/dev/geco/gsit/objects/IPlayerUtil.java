package dev.geco.gsit.objects;

import org.bukkit.Location;
import org.bukkit.entity.*;

import net.md_5.bungee.api.chat.BaseComponent;

public interface IPlayerUtil {

    void teleport(Player P, Location L);

    void teleport(Player P, Location L, boolean D);

    void pos(Entity E, Location L);

    void send(Player P, BaseComponent... M);

}