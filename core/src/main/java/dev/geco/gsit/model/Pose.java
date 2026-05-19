package dev.geco.gsit.model;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Pose {

    void spawn();

    void remove();

    @NotNull Seat getSeat();

    @NotNull Player getPlayer();

    @NotNull PoseType getPoseType();

}