package dev.geco.gsit.util;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;

import dev.geco.gsit.objects.*;

public interface IEntityUtil {

    HashMap<Integer, Entity> getSeatMap();

    void posEntity(Entity Entity, Location Location);

    boolean isLocationValid(Location Location);

    boolean isPlayerSitLocationValid(Entity Holder);

    Entity createSeatEntity(Location Location, Entity Rider, boolean Rotate);

    void removeSeatEntity(Entity Entity);

    UUID createPlayerSeatEntity(Entity Holder, Entity Rider);

    IGPoseSeat createPoseSeatObject(GSeat Seat, Pose Pose);

    IGCrawl createCrawlObject(Player Player);

}