package dev.geco.gsit.util;

import org.bukkit.*;
import org.bukkit.entity.*;

public interface ISpawnUtil {

    boolean isLocationValid(Location Location);

    boolean isPlayerSitLocationValid(Entity Holder);

    Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate);

    void createPlayerSeatEntity(Entity Holder, Entity Rider);

}