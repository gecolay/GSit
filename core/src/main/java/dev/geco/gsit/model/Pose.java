package dev.geco.gsit.model;

import org.bukkit.entity.Player;

public interface Pose {

    void spawn();

    void remove();

    Seat getSeat();

    Player getPlayer();

    PoseType getPoseType();

}