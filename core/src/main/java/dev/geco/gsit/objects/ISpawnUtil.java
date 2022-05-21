package dev.geco.gsit.objects;

import org.bukkit.Location;
import org.bukkit.entity.*;

public interface ISpawnUtil {

    boolean needCheck();

    boolean check(Location Location);

    Entity createEntity(Location Location);

    Entity createEntity(Location Location, Entity Rider);

}