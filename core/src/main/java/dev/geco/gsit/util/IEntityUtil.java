package dev.geco.gsit.util;

import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.IGCrawl;
import dev.geco.gsit.object.IGPose;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

public interface IEntityUtil {

    void setEntityLocation(Entity entity, Location location);

    boolean isSitLocationValid(Location location);

    boolean isPlayerSitLocationValid(Location location);

    Entity createSeatEntity(Location location, Entity entity, boolean canRotate);

    boolean createPlayerSeatEntities(Player player, Player target);

    IGPose createPose(GSeat seat, Pose pose);

    IGCrawl createCrawl(Player player);

}