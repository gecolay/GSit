package dev.geco.gsit.util;

import org.bukkit.*;
import org.bukkit.entity.*;

public interface IPlayerUtil {

    void teleport(Player P, Location L);

    void teleport(Player P, Location L, boolean D);

    void pos(Entity E, Location L);

}