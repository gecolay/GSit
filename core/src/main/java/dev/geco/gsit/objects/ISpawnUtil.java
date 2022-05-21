package dev.geco.gsit.objects;

import org.bukkit.Location;
import org.bukkit.entity.*;

public interface ISpawnUtil {

    boolean needCheck();

    boolean checkLocation(Location Location);

    Entity createSeatEntity(Location Location);

    Entity createSeatEntity(Location Location, Entity Rider);

}