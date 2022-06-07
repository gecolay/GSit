package dev.geco.gsit.objects;

import org.bukkit.Location;
import org.bukkit.entity.*;

public interface ISpawnUtil {

    boolean needCheck();

    boolean checkLocation(Location Location);

    boolean checkPlayerLocation(Entity Holder);

    Entity createSeatEntity(Location Location, Entity Rider);

    Entity createPlayerSeatEntity(Entity Holder, Entity Rider);

}