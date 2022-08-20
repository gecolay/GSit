package dev.geco.gsit.util;

import org.bukkit.*;
import org.bukkit.entity.*;

public interface ISpawnUtil {

    boolean needCheck();

    boolean checkLocation(Location Location);

    boolean checkPlayerLocation(Entity Holder);

    Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate);

    Entity createPlayerSeatEntity(Entity Holder, Entity Rider);

}