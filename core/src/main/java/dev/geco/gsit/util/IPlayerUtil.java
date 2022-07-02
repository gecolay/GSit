package dev.geco.gsit.util;

import org.bukkit.*;
import org.bukkit.entity.*;

public interface IPlayerUtil {

    void teleportPlayer(Player Player, Location Location);

    void teleportPlayer(Player Player, Location Location, boolean Dismount);

    void teleportEntity(Entity Entity, Location Location);

}