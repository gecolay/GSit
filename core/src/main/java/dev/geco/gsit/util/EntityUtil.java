package dev.geco.gsit.util;

import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.Pose;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface EntityUtil {

    void setEntityLocation(Entity entity, Location location);

    boolean isSitLocationValid(Location location);

    boolean isPlayerSitLocationValid(Location location);

    Entity createSeatEntity(Location location, Entity entity, boolean canRotate);

    Set<UUID> createPlayerSitEntities(Player player, Player target);

    Pose createPose(Seat seat, PoseType poseType);

    Crawl createCrawl(Player player);

}