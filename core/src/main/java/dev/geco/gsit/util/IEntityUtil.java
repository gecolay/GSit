package dev.geco.gsit.util;

import dev.geco.gsit.objects.GSeat;
import dev.geco.gsit.objects.IGCrawl;
import dev.geco.gsit.objects.IGPose;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.util.UUID;

public interface IEntityUtil {

    void setEntityLocation(Entity entity, Location location);

    boolean isSitLocationValid(Location location);

    boolean isPlayerSitLocationValid(Location location);

    Entity createSeatEntity(Location location, Entity entity, boolean canRotate);

    UUID createPlayerSeatEntity(Entity holder, Entity entity);

    IGPose createPose(GSeat seat, Pose pose);

    IGCrawl createCrawl(Player player);

}