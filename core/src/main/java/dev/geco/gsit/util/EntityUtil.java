package dev.geco.gsit.util;

import dev.geco.gsit.model.Crawl;
import dev.geco.gsit.model.Pose;
import dev.geco.gsit.model.PoseType;
import dev.geco.gsit.model.Seat;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface EntityUtil {

    void setEntityLocation(@NotNull Entity entity, @NotNull Location location);

    boolean isSitLocationValid(@NotNull Location location);

    boolean isPlayerSitLocationValid(@NotNull Location location);

    @Nullable Entity createSeatEntity(@NotNull Location location, @NotNull Entity entity, boolean canRotate);

    @Nullable List<UUID> createPlayerSitEntities(@NotNull Player player, @NotNull Player target);

    @Nullable Pose createPose(@NotNull Seat seat, @NotNull PoseType poseType);

    @Nullable Crawl createCrawl(@NotNull Player player);

}